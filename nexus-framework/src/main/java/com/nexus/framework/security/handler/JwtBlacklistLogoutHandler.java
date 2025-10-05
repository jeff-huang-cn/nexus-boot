package com.nexus.framework.security.handler;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * JWT黑名单退出处理器
 * 
 * 将退出的JWT加入Redis黑名单，使其立即失效
 * 
 * @author nexus
 */
@Slf4j
@Component
public class JwtBlacklistLogoutHandler implements LogoutHandler {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private JwtDecoder jwtDecoder;

    private static final String BLACKLIST_KEY_PREFIX = "jwt:blacklist:";

    @Override
    public void logout(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) {
        // 1. 从请求头获取JWT
        String token = resolveToken(request);
        if (token == null) {
            log.warn("退出登录时未找到JWT Token");
            return;
        }

        try {
            // 2. 解析JWT
            Jwt jwt = jwtDecoder.decode(token);
            String jti = jwt.getId();

            if (jti == null) {
                log.warn("JWT中没有JTI，无法加入黑名单");
                return;
            }

            // 3. 计算JWT剩余有效时间
            Instant expiresAt = jwt.getExpiresAt();
            if (expiresAt == null) {
                log.warn("JWT中没有过期时间");
                return;
            }

            long ttl = ChronoUnit.SECONDS.between(Instant.now(), expiresAt);

            // 4. 如果JWT还有有效时间，加入黑名单
            if (ttl > 0) {
                String blacklistKey = BLACKLIST_KEY_PREFIX + jti;
                redisTemplate.opsForValue().set(blacklistKey, "1", ttl, TimeUnit.SECONDS);

                log.info("JWT已加入黑名单，JTI: {}, TTL: {}秒", jti, ttl);
            } else {
                log.info("JWT已过期，无需加入黑名单，JTI: {}", jti);
            }

        } catch (Exception e) {
            log.error("处理JWT黑名单时发生错误", e);
        }
    }

    /**
     * 从请求头中提取JWT Token
     * 
     * @param request HTTP请求
     * @return JWT Token字符串，如果没有则返回null
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
