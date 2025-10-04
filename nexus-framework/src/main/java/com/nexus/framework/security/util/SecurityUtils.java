package com.nexus.framework.security.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring Security 工具类
 * 用于获取当前登录用户信息
 *
 * @author nexus
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户的认证对象
     *
     * @return Authentication 认证对象，未登录返回null
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取当前登录用户名
     *
     * @return 用户名，未登录返回null
     */
    public static String getUsername() {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return principal != null ? principal.toString() : null;
    }

    /**
     * 获取当前登录用户的UserDetails
     *
     * @return UserDetails对象，未登录或类型不匹配返回null
     */
    public static UserDetails getUserDetails() {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return (UserDetails) principal;
        }
        return null;
    }

    /**
     * 获取当前登录用户的权限列表
     *
     * @return 权限字符串集合
     */
    public static Set<String> getAuthorities() {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return Set.of();
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

    /**
     * 判断当前用户是否已登录
     *
     * @return true=已登录，false=未登录
     */
    public static boolean isAuthenticated() {
        Authentication authentication = getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * 判断当前用户是否拥有指定权限
     *
     * @param authority 权限标识
     * @return true=拥有，false=不拥有
     */
    public static boolean hasAuthority(String authority) {
        return getAuthorities().contains(authority);
    }

    /**
     * 判断当前用户是否拥有任意一个权限
     *
     * @param authorities 权限标识数组
     * @return true=拥有任意一个，false=都不拥有
     */
    public static boolean hasAnyAuthority(String... authorities) {
        Set<String> userAuthorities = getAuthorities();
        for (String authority : authorities) {
            if (userAuthorities.contains(authority)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断当前用户是否拥有所有权限
     *
     * @param authorities 权限标识数组
     * @return true=拥有所有，false=不拥有所有
     */
    public static boolean hasAllAuthorities(String... authorities) {
        Set<String> userAuthorities = getAuthorities();
        for (String authority : authorities) {
            if (!userAuthorities.contains(authority)) {
                return false;
            }
        }
        return true;
    }
}
