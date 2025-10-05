package com.nexus.framework.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "nexus.security.jwk")
public class JwkProperties {

    /**
     * JWK有效期（天）
     */
    private int validityDays = 90;

    /**
     * 密钥轮换提前期（天）
     * 在密钥过期前N天开始使用新密钥签发JWT，但旧密钥仍可验证
     */
    private int rotationAdvanceDays = 7;

    /**
     * Redis缓存过期时间（小时）
     */
    private int cacheExpireHours = 24;

    /**
     * 是否启用自动密钥轮换
     */
    private boolean autoRotationEnabled = true;
}
