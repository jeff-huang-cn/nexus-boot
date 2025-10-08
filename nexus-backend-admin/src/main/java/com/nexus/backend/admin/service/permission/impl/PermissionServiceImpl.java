package com.nexus.backend.admin.service.permission.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexus.backend.admin.dal.dataobject.permission.MenuDO;
import com.nexus.backend.admin.dal.dataobject.permission.RoleMenuDO;
import com.nexus.backend.admin.dal.dataobject.permission.UserRoleDO;
import com.nexus.backend.admin.dal.mapper.permission.MenuMapper;
import com.nexus.backend.admin.dal.mapper.permission.RoleMenuMapper;
import com.nexus.backend.admin.dal.mapper.permission.UserRoleMapper;
import com.nexus.backend.admin.enums.CommonStatusEnum;
import com.nexus.backend.admin.enums.MenuTypeEnum;
import com.nexus.backend.admin.service.permission.PermissionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限聚合服务实现类
 *
 * @author nexus
 */
@Slf4j
@Service
public class PermissionServiceImpl implements PermissionService {

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private RoleMenuMapper roleMenuMapper;

    @Resource
    private MenuMapper menuMapper;

    @Override
    public Set<String> getUserAllPermissions(Long userId) {
        if (userId == null) {
            return Collections.emptySet();
        }

        // 1. 查询用户的所有角色ID
        List<Long> roleIds = getUserRoleIds(userId);
        if (CollectionUtils.isEmpty(roleIds)) {
            log.debug("用户 {} 没有分配角色", userId);
            return Collections.emptySet();
        }

        // 2. 查询角色分配的所有菜单ID（包括目录、菜单、按钮）
        List<Long> menuIds = getRoleMenuIds(roleIds);
        if (CollectionUtils.isEmpty(menuIds)) {
            log.debug("用户 {} 的角色没有分配菜单", userId);
            return Collections.emptySet();
        }

        // 3. 从已分配的菜单ID中，过滤出按钮权限
        // 注意：只有角色明确分配的按钮才有权限，不是父菜单下的所有按钮都有权限
        List<MenuDO> buttons = menuMapper.selectList(
                new LambdaQueryWrapper<MenuDO>()
                        .select(MenuDO::getPermission)
                        .in(MenuDO::getId, menuIds) // 直接查询已分配的菜单ID
                        .eq(MenuDO::getType, MenuTypeEnum.BUTTON.getValue()) // 类型=按钮
                        .eq(MenuDO::getStatus, CommonStatusEnum.ENABLE.getValue()) // 状态=启用
                        .isNotNull(MenuDO::getPermission) // 权限标识不为空
        );

        // 4. 提取权限标识
        Set<String> permissions = buttons.stream()
                .map(MenuDO::getPermission)
                .filter(p -> p != null && !p.trim().isEmpty())
                .collect(Collectors.toSet());

        log.debug("用户 {} 拥有 {} 个按钮权限: {}", userId, permissions.size(), permissions);
        return permissions;
    }

    @Override
    public boolean hasPermission(Long userId, String permission) {
        if (userId == null || permission == null || permission.trim().isEmpty()) {
            return false;
        }

        Set<String> userPermissions = getUserAllPermissions(userId);
        return userPermissions.contains(permission);
    }

    @Override
    public boolean hasAnyPermission(Long userId, String... permissions) {
        if (userId == null || permissions == null || permissions.length == 0) {
            return false;
        }

        Set<String> userPermissions = getUserAllPermissions(userId);
        for (String permission : permissions) {
            if (userPermissions.contains(permission)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasAllPermissions(Long userId, String... permissions) {
        if (userId == null || permissions == null || permissions.length == 0) {
            return false;
        }

        Set<String> userPermissions = getUserAllPermissions(userId);
        for (String permission : permissions) {
            if (!userPermissions.contains(permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取用户的所有角色ID
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    private List<Long> getUserRoleIds(Long userId) {
        List<UserRoleDO> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRoleDO>()
                        .select(UserRoleDO::getRoleId)
                        .eq(UserRoleDO::getUserId, userId));

        return userRoles.stream()
                .map(UserRoleDO::getRoleId)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 获取角色分配的所有菜单ID
     *
     * @param roleIds 角色ID列表
     * @return 菜单ID列表
     */
    private List<Long> getRoleMenuIds(List<Long> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return Collections.emptyList();
        }

        List<RoleMenuDO> roleMenus = roleMenuMapper.selectList(
                new LambdaQueryWrapper<RoleMenuDO>()
                        .select(RoleMenuDO::getMenuId)
                        .in(RoleMenuDO::getRoleId, roleIds));

        return roleMenus.stream()
                .map(RoleMenuDO::getMenuId)
                .distinct()
                .collect(Collectors.toList());
    }
}
