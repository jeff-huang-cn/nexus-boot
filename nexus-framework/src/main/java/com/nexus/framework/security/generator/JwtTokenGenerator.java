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

        // 提取用户权限为列表（前端需要数组格式）
        java.util.List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        System.out.println("=== 生成JWT Token ===");
        System.out.println("用户名: " + authentication.getName());
        System.out.println("权限列表: " + authorities);
        System.out.println("权限类型: " + authorities.getClass().getName());

        // 构建JWT声明（Claims）
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("nexus-app") // 发行者
                .issuedAt(now) // 发行时间
                .expiresAt(now.plus(2, ChronoUnit.HOURS)) // 过期时间（2小时）
                .subject(authentication.getName()) // 用户名
                .claim("authorities", authorities) // 权限信息（List格式，Spring会自动转换为JSON数组）
                .build();

        System.out.println("JWT Claims: " + claims.getClaims());

        // 生成并返回JWT令牌
        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        System.out.println("生成的Token长度: " + token.length());

        return token;
    }
}
