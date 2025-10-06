package com.nexus.framework.security.generator;

import com.nexus.framework.security.model.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenGenerator {

    private final JwtEncoder jwtEncoder;

    public String generateToken(Authentication authentication) {
        Instant now = Instant.now();

        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUserId();

        // 生成唯一的JWT ID（用于黑名单机制）
        String jti = UUID.randomUUID().toString();

        log.info("生成JWT Token，用户ID: {}, JTI: {}", userId, jti);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("nexus-app")
                .issuedAt(now)
                .expiresAt(now.plus(2, ChronoUnit.HOURS))
                .subject(authentication.getName())
                .id(jti) // JWT唯一标识
                .claim("userId", userId)
                .build();
        JwtEncoderParameters jwtEncoderParameters = JwtEncoderParameters.from(claims);
        Jwt encode = jwtEncoder.encode(jwtEncoderParameters);
        String token = encode.getTokenValue();
        log.info("生成JWT Token成功，用户ID: {}, JTI: {}", userId, jti);
        return token;
    }
}
