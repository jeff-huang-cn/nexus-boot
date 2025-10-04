package com.nexus.backend.admin.service.permission;

import java.util.Set;

/**
 * 权限聚合服务
 * 用于查询用户的权限信息
 *
 * @author nexus
 */
public interface PermissionService {

    /**
     * 获取用户的所有按钮权限
     *
     * @param userId 用户ID
     * @return 用户的所有按钮权限标识集合，如：["system:user:create", "system:user:update"]
     */
    Set<String> getUserAllPermissions(Long userId);

    /**
     * 判断用户是否拥有指定权限
     *
     * @param userId     用户ID
     * @param permission 权限标识
     * @return true=拥有权限，false=没有权限
     */
    boolean hasPermission(Long userId, String permission);

    /**
     * 判断用户是否拥有任意一个权限
     *
     * @param userId      用户ID
     * @param permissions 权限标识数组
     * @return true=拥有任意一个权限，false=都没有权限
     */
    boolean hasAnyPermission(Long userId, String... permissions);

    /**
     * 判断用户是否拥有所有权限
     *
     * @param userId      用户ID
     * @param permissions 权限标识数组
     * @return true=拥有所有权限，false=缺少任意权限
     */
    boolean hasAllPermissions(Long userId, String... permissions);
}
