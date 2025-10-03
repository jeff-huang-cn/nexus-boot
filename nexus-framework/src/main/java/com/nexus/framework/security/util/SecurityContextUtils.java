package com.nexus.framework.security.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Objects;

/**
 * Security 上下文工具类
 * 用于获取当前登录用户信息
 * 
 * <p>
 * 参考 Yudao 项目 SecurityFrameworkUtils 实现
 * </p>
 * 
 * @author nexus
 */
@Slf4j
public class SecurityContextUtils {

    /**
     * 获取当前登录用户ID（Long类型）
     * 
     * @return 用户ID，未登录或获取失败返回 null
     */
    public static Long getLoginUserId() {
        String userIdStr = getLoginUserIdAsString();
        if (userIdStr == null) {
            return null;
        }

        try {
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            log.warn("用户ID格式转换失败: {}", userIdStr, e);
            return null;
        }
    }

    /**
     * 获取当前登录用户ID（String类型）
     * 
     * <p>
     * 获取逻辑：
     * 1. 从 SecurityContext 获取 Authentication
     * 2. 从 Authentication 获取 Principal
     * 3. 尝试从 Principal 中提取用户ID
     * </p>
     * 
     * @return 用户ID字符串，未登录或获取失败返回 null
     */
    public static String getLoginUserIdAsString() {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            log.debug("未找到认证信息，可能未登录或在非Web环境");
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal == null) {
            log.debug("Principal 为空");
            return null;
        }

        // 场景1: Principal 是自定义的 LoginUser 对象（包含 userId 字段）
        // 通过反射尝试获取 userId 或 id 字段
        try {
            // 尝试获取 userId 字段
            java.lang.reflect.Field userIdField = principal.getClass().getDeclaredField("userId");
            userIdField.setAccessible(true);
            Object userId = userIdField.get(principal);
            if (userId != null) {
                return userId.toString();
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // 字段不存在或访问失败，尝试下一个方式
        }

        try {
            // 尝试获取 id 字段
            java.lang.reflect.Field idField = principal.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            Object id = idField.get(principal);
            if (id != null) {
                return id.toString();
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // 字段不存在或访问失败，继续
        }

        // 场景2: Principal 是 UserDetails 对象
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            // UserDetails 的 username 可能是用户ID或用户名
            // 这里假设 username 是用户ID（可根据实际情况调整）
            return userDetails.getUsername();
        }

        // 场景3: Principal 是字符串（用户名或用户ID）
        if (principal instanceof String) {
            return (String) principal;
        }

        // 场景4: Principal 是数字（用户ID）
        if (principal instanceof Number) {
            return principal.toString();
        }

        log.warn("无法从 Principal 中提取用户ID，Principal类型: {}", principal.getClass().getName());
        return null;
    }

    /**
     * 获取当前登录用户名
     * 
     * @return 用户名，未登录或获取失败返回 null
     */
    public static String getLoginUsername() {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal == null) {
            return null;
        }

        // UserDetails 对象
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }

        // 字符串
        if (principal instanceof String) {
            return (String) principal;
        }

        // 尝试通过反射获取 username 字段
        try {
            java.lang.reflect.Field usernameField = principal.getClass().getDeclaredField("username");
            usernameField.setAccessible(true);
            Object username = usernameField.get(principal);
            if (username != null) {
                return username.toString();
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // 忽略
        }

        return null;
    }

    /**
     * 获取当前认证信息
     * 
     * @return Authentication 对象，未登录返回 null
     */
    private static Authentication getAuthentication() {
        try {
            SecurityContext context = SecurityContextHolder.getContext();
            if (context == null) {
                return null;
            }

            Authentication authentication = context.getAuthentication();
            if (authentication == null) {
                return null;
            }

            // 检查是否已认证
            if (!authentication.isAuthenticated()) {
                return null;
            }

            // 排除匿名用户
            if ("anonymousUser".equals(authentication.getPrincipal())) {
                return null;
            }

            return authentication;
        } catch (Exception e) {
            log.warn("获取认证信息失败", e);
            return null;
        }
    }

    /**
     * 判断当前是否已登录
     * 
     * @return true=已登录，false=未登录
     */
    public static boolean isAuthenticated() {
        return getAuthentication() != null;
    }
}
