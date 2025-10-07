package com.nexus.framework.security.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.nexus.framework.security.config.JwkProperties;
import com.nexus.framework.security.dal.dataobject.JwkDO;
import com.nexus.framework.security.dal.mapper.JwkMapper;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JwkService {

    @Resource
    private JwkMapper jwkMapper;
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private JwkProperties jwkProperties;

    @Resource
    @Lazy
    private JwkService jwkService;

    private static final String JWK_CACHE_KEY = "oauth2:jwk:active";
    private static final String JWK_CREATE_LOCK_KEY = "oauth2:jwk:create:lock";

    public JWKSet getSigningJwkSet() {
        return loadFromDatabaseWithoutLock();
    }

    /**
     * 获取活跃的JWK集合（混合模式：Redis缓存 + 数据库持久化）
     * 
     * 流程：
     * 1. 尝试从Redis缓存读取
     * 2. 缓存miss → 获取分布式锁
     * 3. 双重检查Redis（可能其他线程已缓存）
     * 4. Redis还是miss → 查询数据库
     * 5. 数据库为空 → 创建新JWK
     * 6. 缓存到Redis
     * 7. 释放锁
     */
    public JWKSet getVerificationJwkSet() {
        try {
            String cachedJwkSet = redisTemplate.opsForValue().get(JWK_CACHE_KEY);
            if (cachedJwkSet != null) {
                log.debug("从Redis缓存加载JWK");
                return JWKSet.parse(cachedJwkSet);
            }
        } catch (Exception e) {
            log.warn("从Redis加载JWK失败: {}", e.getMessage());
        }

        // Redis缓存miss，获取分布式锁避免并发加载
        log.info("Redis缓存miss，尝试获取分布式锁");
        RLock lock = redissonClient.getLock(JWK_CREATE_LOCK_KEY);

        try {
            // 尝试获取锁，最多等待10秒，锁持有时间30秒
            boolean locked = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (!locked) {
                log.warn("获取分布式锁超时，直接查询数据库");
                return loadFromDatabaseWithoutLock();
            }

            try {
                // 双重检查：获取锁后再次检查Redis缓存
                // 可能其他线程已经加载并缓存了
                log.debug("获取分布式锁成功，进行双重检查Redis");
                String cachedJwkSet = redisTemplate.opsForValue().get(JWK_CACHE_KEY);
                if (cachedJwkSet != null) {
                    log.info("其他线程已缓存JWK到Redis，无需重复加载");
                    return JWKSet.parse(cachedJwkSet);
                }

                // Redis仍然miss，从数据库加载
                log.info("双重检查后Redis仍miss，从数据库加载JWK");
                JWKSet jwkSet = loadFromDatabaseWithoutLock();
                cacheJwkSet(jwkSet);
                return jwkSet;

            } finally {
                lock.unlock();
                log.debug("释放JWK加载分布式锁");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁时被中断，降级到直接查询数据库");
            return loadFromDatabaseWithoutLock();
        } catch (Exception e) {
            log.error("获取JWK失败", e);
            throw new RuntimeException("无法加载JWK", e);
        }
    }

    /**
     * 从数据库加载JWK集合
     */
    private JWKSet loadFromDatabaseWithoutLock() {
        List<JwkDO> activeJwks = jwkMapper.selectList(
                new LambdaQueryWrapper<JwkDO>()
                        .eq(JwkDO::getIsActive, true)
                        .gt(JwkDO::getExpiresAt, LocalDateTime.now())
                        .orderByDesc(JwkDO::getCreatedTime)); // 最新的JWK排在最前面

        if (activeJwks.isEmpty()) {
            log.warn("数据库中没有活跃的JWK，创建新的JWK");
            JwkDO newJwk = jwkService.createAndSaveJwk();
            activeJwks = List.of(newJwk);
        }

        List<JWK> jwks = activeJwks.stream()
                .map(this::convertToJwk)
                .toList();

        return new JWKSet(jwks);
    }

    /**
     * 缓存JWK集合到Redis
     * 注意：必须使用toString(true)来包含私钥，否则JWT签名会失败
     */
    private void cacheJwkSet(JWKSet jwkSet) {
        try {
            String jwkSetJson = jwkSet.toString(true);

            redisTemplate.opsForValue().set(
                    JWK_CACHE_KEY,
                    jwkSetJson,
                    jwkProperties.getCacheExpireHours(),
                    TimeUnit.HOURS);
            log.info("JWK已缓存到Redis（包含私钥），过期时间: {}小时", jwkProperties.getCacheExpireHours());
        } catch (Exception e) {
            log.error("缓存JWK到Redis失败，但不影响正常使用: {}", e.getMessage());
        }
    }

    /**
     * 创建并保存新的JWK（保存到数据库 + Redis）
     */
    @Transactional(rollbackFor = Exception.class)
    public JwkDO createAndSaveJwk() {
        try {
            KeyPair keyPair = generateRsaKey();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

            JwkDO jwkDO = new JwkDO();
            jwkDO.setKeyId(UUID.randomUUID().toString());
            jwkDO.setKeyType("RSA");
            jwkDO.setAlgorithm("RS256");
            jwkDO.setPublicKey(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
            jwkDO.setPrivateKey(Base64.getEncoder().encodeToString(privateKey.getEncoded()));
            jwkDO.setCreatedTime(LocalDateTime.now());
            jwkDO.setExpiresAt(LocalDateTime.now().plusDays(jwkProperties.getValidityDays()));
            jwkDO.setIsActive(true);

            jwkMapper.insert(jwkDO);
            log.info("成功创建并保存JWK到数据库，keyId={}, 过期时间={}天",
                    jwkDO.getKeyId(), jwkProperties.getValidityDays());

            // 清除缓存，下次验证时会加载包含新密钥的JWKSet
            clearCache();

            return jwkDO;
        } catch (Exception e) {
            log.error("创建JWK失败", e);
            throw new RuntimeException("创建JWK失败", e);
        }
    }

    /**
     * 生成RSA密钥对
     */
    private KeyPair generateRsaKey() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("生成RSA密钥对失败", e);
        }
    }

    /**
     * 将数据库JWK对象转换为Nimbus JWK对象
     */
    private JWK convertToJwk(JwkDO jwkDO) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            byte[] publicKeyBytes = Base64.getDecoder().decode(jwkDO.getPublicKey());
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);

            byte[] privateKeyBytes = Base64.getDecoder().decode(jwkDO.getPrivateKey());
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);

            return new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(jwkDO.getKeyId())
                    .build();
        } catch (Exception e) {
            log.error("转换JWK失败，keyId={}", jwkDO.getKeyId(), e);
            throw new RuntimeException("转换JWK失败", e);
        }
    }

    /**
     * 清除JWK缓存（用于密钥轮换时）
     */
    public void clearCache() {
        try {
            redisTemplate.delete(JWK_CACHE_KEY);
            log.info("已清除JWK缓存");
        } catch (Exception e) {
            log.error("清除JWK缓存失败: {}", e.getMessage());
        }
    }

    /**
     * 禁用过期的JWK
     */

    @Transactional(rollbackFor = Exception.class)
    public void deactivateExpiredKeys() {
        List<JwkDO> expiredJwks = jwkMapper.selectList(
                new LambdaQueryWrapper<JwkDO>()
                        .eq(JwkDO::getIsActive, true)
                        .lt(JwkDO::getExpiresAt, LocalDateTime.now()));

        if (!expiredJwks.isEmpty()) {
            expiredJwks.forEach(jwk -> {
                jwk.setIsActive(false);
                jwkMapper.updateById(jwk);
                log.info("已禁用过期的JWK，keyId={}", jwk.getKeyId());
            });
            clearCache();
        }
    }
}
