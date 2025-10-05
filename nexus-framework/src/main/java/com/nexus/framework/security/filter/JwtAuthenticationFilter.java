package com.nexus.framework.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT验证过滤器：拦截请求并验证JWT令牌
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;

    public JwtAuthenticationFilter(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        // 1. 从请求头获取JWT令牌（格式：Bearer <token>）
        String token = resolveToken(request);

        log.info("=== JwtAuthenticationFilter ===");
        log.info("URI: {}", request.getRequestURI());
        log.info("Token存在: {}", token != null);

        if (token != null) {
            try {
                // 2. 解码并验证JWT
                Jwt jwt = jwtDecoder.decode(token);
                log.info("JWT解码成功，用户: {}", jwt.getSubject());
                log.info("JWT Claims: {}", jwt.getClaims());

                // 3. 从JWT中提取权限信息
                Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
                log.info("提取到的权限: {}", authorities);

                // 4. 创建认证令牌并设置权限
                JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.info("JWT认证成功: 用户={}, 权限数量={}", jwt.getSubject(), authorities.size());
            } catch (Exception e) {
                // JWT无效，清空上下文
                log.error("JWT验证失败: {}", e.getMessage(), e);
                SecurityContextHolder.clearContext();
            }
        } else {
            log.warn("请求头中没有JWT Token");
        }

        // 继续执行过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中解析JWT令牌
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // 去掉"Bearer "前缀
        }
        return null;
    }

    /**
     * 从JWT中提取权限信息
     */
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        try {
            // 尝试从JWT的claims中获取authorities
            Object authoritiesObj = jwt.getClaim("authorities");
            log.info("JWT中authorities的原始类型: {}", authoritiesObj != null ? authoritiesObj.getClass().getName() : "null");
            log.info("JWT中authorities的原始值: {}", authoritiesObj);

            // 尝试多种格式
            List<String> authoritiesList = null;

            // 1. 尝试作为StringList
            try {
                authoritiesList = jwt.getClaimAsStringList("authorities");
                log.info("作为StringList提取成功: {}", authoritiesList);
            } catch (Exception e) {
                log.warn("无法作为StringList提取: {}", e.getMessage());
            }

            // 2. 如果失败，尝试作为String（空格分隔）
            if (authoritiesList == null || authoritiesList.isEmpty()) {
                try {
                    String authStr = jwt.getClaimAsString("authorities");
                    if (authStr != null && !authStr.isEmpty()) {
                        authoritiesList = List.of(authStr.split(" "));
                        log.info("作为空格分隔String提取成功: {}", authoritiesList);
                    }
                } catch (Exception e) {
                    log.warn("无法作为String提取: {}", e.getMessage());
                }
            }

            if (authoritiesList != null && !authoritiesList.isEmpty()) {
                return authoritiesList.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("提取JWT权限失败: {}", e.getMessage(), e);
        }

        log.warn("未能提取到任何权限，返回空列表");
        return Collections.emptyList();
    }
}
