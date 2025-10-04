package com.nexus.framework.security.generator;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Component
public class JwtTokenGenerator {

    private final JwtEncoder jwtEncoder;

    // 注入JWT编码器
    public JwtTokenGenerator(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    /**
     * 根据认证信息生成JWT令牌（包含用户名和权限）
     */
    public String generateToken(Authentication authentication) {
        Instant now = Instant.now();
        
        // 提取用户权限（如：ROLE_ADMIN, ROLE_USER）
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" ")); // 用空格分隔多个权限
        
        // 构建JWT声明（Claims）
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("my-app") // 发行者（自定义）
                .issuedAt(now) // 发行时间
                .expiresAt(now.plus(2, ChronoUnit.HOURS)) // 过期时间（2小时）
                .subject(authentication.getName()) // 用户名
                .claim("authorities", authorities) // 权限信息（关键：前端需用此判断按钮权限）
                .build();
        
        // 生成并返回JWT令牌
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
