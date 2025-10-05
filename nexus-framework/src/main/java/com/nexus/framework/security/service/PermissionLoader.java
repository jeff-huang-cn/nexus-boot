package com.nexus.framework.security.service;

import java.util.Set;

public interface PermissionLoader {

    /**
     * 根据用户ID加载用户所有权限
     */
    Set<String> loadUserPermissions(Long userId);
}
