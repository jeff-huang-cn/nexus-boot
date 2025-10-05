package com.nexus.framework.security.filter;

import com.nexus.framework.security.service.PermissionLoader;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
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
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;
    private final PermissionLoader permissionLoader;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String BLACKLIST_KEY_PREFIX = "jwt:blacklist:";

    public JwtAuthenticationFilter(JwtDecoder jwtDecoder,
            PermissionLoader permissionLoader,
            RedisTemplate<String, String> redisTemplate) {
        this.jwtDecoder = jwtDecoder;
        this.permissionLoader = permissionLoader;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null) {
            try {
                Jwt jwt = jwtDecoder.decode(token);

                // 检查JWT是否在黑名单中
                String jti = jwt.getId();
                if (jti != null && isTokenBlacklisted(jti)) {
                    log.warn("JWT在黑名单中，拒绝认证，JTI: {}", jti);
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }

                Long userId = jwt.getClaim("userId");
                log.debug("JWT验证成功，用户: {}, userId: {}", jwt.getSubject(), userId);

                Collection<GrantedAuthority> authorities = loadAuthorities(userId);
                log.debug("从数据库加载权限，数量: {}", authorities.size());

                JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                log.error("JWT验证失败: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private Collection<GrantedAuthority> loadAuthorities(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        try {
            Set<String> permissions = permissionLoader.loadUserPermissions(userId);
            return permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("加载用户权限失败，userId: {}", userId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 检查JWT是否在黑名单中
     * 
     * @param jti JWT唯一标识
     * @return true=在黑名单中（已撤销），false=不在黑名单中（有效）
     */
    private boolean isTokenBlacklisted(String jti) {
        try {
            String blacklistKey = BLACKLIST_KEY_PREFIX + jti;
            return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
        } catch (Exception e) {
            log.error("检查JWT黑名单时发生错误，JTI: {}", jti, e);
            // 出错时保守处理：不拒绝请求（降级）
            return false;
        }
    }
}
