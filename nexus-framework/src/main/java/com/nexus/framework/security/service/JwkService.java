package com.nexus.framework.security.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexus.framework.security.config.JwkProperties;
import com.nexus.framework.security.dal.dataobject.JwkDO;
import com.nexus.framework.security.dal.mapper.JwkMapper;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
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
@RequiredArgsConstructor
public class JwkService {

    private final JwkMapper jwkMapper;
    private final RedisTemplate<String, String> redisTemplate;
    private final JwkProperties jwkProperties;

    private static final String JWK_CACHE_KEY = "oauth2:jwk:active";

    /**
     * 获取活跃的JWK集合（混合模式：Redis缓存 + 数据库持久化）
     */
    public JWKSet getJwkSet() {
        try {
            String cachedJwkSet = redisTemplate.opsForValue().get(JWK_CACHE_KEY);
            if (cachedJwkSet != null) {
                log.debug("从Redis缓存加载JWK");
                return JWKSet.parse(cachedJwkSet);
            }
        } catch (Exception e) {
            log.warn("从Redis加载JWK失败，降级到数据库: {}", e.getMessage());
        }

        log.info("Redis缓存miss，从数据库加载JWK");
        JWKSet jwkSet = loadFromDatabase();
        cacheJwkSet(jwkSet);
        return jwkSet;
    }

    /**
     * 从数据库加载JWK集合
     */
    private JWKSet loadFromDatabase() {
        List<JwkDO> activeJwks = jwkMapper.selectList(
                new LambdaQueryWrapper<JwkDO>()
                        .eq(JwkDO::getIsActive, true)
                        .gt(JwkDO::getExpiresAt, LocalDateTime.now()));

        if (activeJwks.isEmpty()) {
            log.warn("数据库中没有活跃的JWK，创建新的JWK");
            JwkDO newJwk = createAndSaveJwk();
            activeJwks = List.of(newJwk);
        }

        List<JWK> jwks = activeJwks.stream()
                .map(this::convertToJwk)
                .collect(Collectors.toList());

        return new JWKSet(jwks);
    }

    /**
     * 缓存JWK集合到Redis
     */
    private void cacheJwkSet(JWKSet jwkSet) {
        try {
            redisTemplate.opsForValue().set(
                    JWK_CACHE_KEY,
                    jwkSet.toString(),
                    jwkProperties.getCacheExpireHours(),
                    TimeUnit.HOURS);
            log.info("JWK已缓存到Redis，过期时间: {}小时", jwkProperties.getCacheExpireHours());
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
     * 检查是否需要轮换密钥
     */
    public boolean needsRotation() {
        if (!jwkProperties.isAutoRotationEnabled()) {
            return false;
        }

        List<JwkDO> activeJwks = jwkMapper.selectList(
                new LambdaQueryWrapper<JwkDO>()
                        .eq(JwkDO::getIsActive, true)
                        .gt(JwkDO::getExpiresAt, LocalDateTime.now()));

        if (activeJwks.isEmpty()) {
            return true;
        }

        LocalDateTime rotationThreshold = LocalDateTime.now()
                .plusDays(jwkProperties.getRotationAdvanceDays());

        return activeJwks.stream()
                .allMatch(jwk -> jwk.getExpiresAt().isBefore(rotationThreshold));
    }

    /**
     * 手动轮换密钥
     */
    @Transactional(rollbackFor = Exception.class)
    public void rotateKey() {
        log.info("开始轮换JWK密钥");

        createAndSaveJwk();
        clearCache();

        log.info("JWK密钥轮换完成");
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
