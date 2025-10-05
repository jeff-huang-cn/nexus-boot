package com.nexus.backend.admin.service.security;

import com.nexus.backend.admin.service.permission.PermissionService;
import com.nexus.framework.security.service.PermissionLoader;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class PermissionLoaderImpl implements PermissionLoader {

    @Resource
    private PermissionService permissionService;

    @Override
    public Set<String> loadUserPermissions(Long userId) {
        return permissionService.getUserAllPermissions(userId);
    }
}
