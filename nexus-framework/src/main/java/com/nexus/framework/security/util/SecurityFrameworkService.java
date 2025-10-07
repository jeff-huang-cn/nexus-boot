package com.nexus.framework.security.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * 权限校验工具类
 * 
 * 用于在 @PreAuthorize 中进行权限校验，并提供日志记录功能
 * 
 * 使用示例：
 * @PreAuthorize("@ss.hasPermission('system:menu:create')")
 * 
 * @author nexus
 */
@Slf4j
@Component("ss")
public class SecurityFrameworkService {

    /**
     * 判断当前用户是否拥有指定权限
     * 
     * @param permission 权限标识符
     * @return 是否拥有权限
     */
    public boolean hasPermission(String permission) {
        return hasAnyPermission(permission);
    }

    /**
     * 判断当前用户是否拥有任意一个指定权限
     * 
     * @param permissions 权限标识符列表
     * @return 是否拥有任意一个权限
     */
    public boolean hasAnyPermission(String... permissions) {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            log.warn("权限校验失败：用户未登录");
            return false;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities == null || authorities.isEmpty()) {
            log.warn("权限校验失败：用户 [{}] 没有任何权限", authentication.getName());
            return false;
        }

        // 检查是否拥有任意一个权限
        for (String permission : permissions) {
            for (GrantedAuthority authority : authorities) {
                if (authority.getAuthority().equals(permission)) {
                    return true;
                }
            }
        }

        // 未通过，打印日志
        log.warn("权限校验失败：用户 [{}] 缺少权限 {}，当前拥有权限：{}",
                authentication.getName(),
                String.join(" 或 ", permissions),
                authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList());
        return false;
    }

    /**
     * 判断当前用户是否拥有所有指定权限
     * 
     * @param permissions 权限标识符列表
     * @return 是否拥有所有权限
     */
    public boolean hasAllPermissions(String... permissions) {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            log.warn("权限校验失败：用户未登录");
            return false;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities == null || authorities.isEmpty()) {
            log.warn("权限校验失败：用户 [{}] 没有任何权限", authentication.getName());
            return false;
        }

        // 检查是否拥有所有权限
        for (String permission : permissions) {
            boolean hasPermission = authorities.stream()
                    .anyMatch(authority -> authority.getAuthority().equals(permission));

            if (!hasPermission) {
                log.warn("权限校验失败：用户 [{}] 缺少权限 [{}]，当前拥有权限：{}",
                        authentication.getName(),
                        permission,
                        authorities.stream()
                                .map(GrantedAuthority::getAuthority)
                                .toList());
                return false;
            }
        }
        return true;
    }

    /**
     * 获取当前认证信息
     */
    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
