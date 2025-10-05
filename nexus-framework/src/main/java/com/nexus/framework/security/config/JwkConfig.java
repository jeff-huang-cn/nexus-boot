package com.nexus.framework.security.config;

import com.nexus.framework.security.service.JwkService;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JWK配置类
 * 
 * 职责：
 * 1. 提供JWKSource Bean供Spring Security使用
 * 2. 实现数据库持久化 + Redis缓存的混合模式
 * 3. 使用分布式锁避免并发创建多个JWK
 * 
 * 
 * @author nexus
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class JwkConfig {

    private final JwkService jwkService;

    /**
     * 提供JWKSource Bean供Spring Security使用
     * 
     * 使用延迟加载策略：
     * 1. 每次调用时从 JwkService 获取最新的 JWK
     * 2. JwkService 优先从 Redis 缓存读取
     * 3. 缓存 miss 时从数据库加载并更新缓存
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        log.info("创建JWKSource Bean（数据库持久化模式）");
        return (jwkSelector, context) -> {
            try {
                return jwkSelector.select(jwkService.getJwkSet());
            } catch (Exception e) {
                log.error("获取JWK失败", e);
                throw new RuntimeException("无法加载JWK，JWT功能不可用", e);
            }
        };
    }
}