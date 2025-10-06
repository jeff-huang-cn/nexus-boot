package com.nexus.framework.security.config;

import com.nexus.framework.security.service.JwkService;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JWK配置类
 * 
 * 职责：
 * 1. 提供两个独立的 JWKSource Bean
 * - signingJwkSource: 用于 JWT 签发（包含私钥，从数据库）
 * - verificationJwkSource: 用于 JWT 验证（只含公钥，从 Redis 缓存）
 * 2. 实现职责分离和性能优化
 * 
 * @author nexus
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class JwkConfig {

    private final JwkService jwkService;

    /**
     * 用于签发 JWT 的 JWKSource（包含私钥）
     * 
     * 使用场景：JwtEncoder 调用
     * 数据源：数据库（每次加载最新）
     * 包含：完整密钥对（公钥+私钥）
     */
    @Bean
    @Qualifier("signingJwkSource")
    public JWKSource<SecurityContext> signingJwkSource() {
        return (jwkSelector, context) -> {
            try {
                return jwkSelector.select(jwkService.getSigningJwkSet());
            } catch (Exception e) {
                log.error("获取签发JWK失败", e);
                throw new RuntimeException("无法加载签发JWK", e);
            }
        };
    }

    /**
     * 用于验证 JWT 的 JWKSource（只包含公钥）
     * 
     * 使用场景：JwtDecoder 调用
     * 数据源：Redis 缓存（降级到数据库）
     * 包含：只有公钥
     */
    @Bean
    @Qualifier("verificationJwkSource")
    public JWKSource<SecurityContext> verificationJwkSource() {
        return (jwkSelector, context) -> {
            try {
                return jwkSelector.select(jwkService.getVerificationJwkSet());
            } catch (Exception e) {
                log.error("获取验证JWK失败", e);
                throw new RuntimeException("无法加载验证JWK", e);
            }
        };
    }
}