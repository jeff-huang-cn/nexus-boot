package com.nexus.framework.security.filter;

import com.nexus.framework.security.service.PermissionLoader;
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
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;
    private final PermissionLoader permissionLoader;

    public JwtAuthenticationFilter(JwtDecoder jwtDecoder, PermissionLoader permissionLoader) {
        this.jwtDecoder = jwtDecoder;
        this.permissionLoader = permissionLoader;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null) {
            try {
                Jwt jwt = jwtDecoder.decode(token);
                Long userId = jwt.getClaim("userId");

                log.info("JWT验证成功，用户: {}, userId: {}", jwt.getSubject(), userId);

                Collection<GrantedAuthority> authorities = loadAuthorities(userId);
                log.info("从数据库加载权限，数量: {}", authorities.size());

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
}
