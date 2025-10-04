package com.nexus.backend.admin.service.security;

import com.nexus.backend.admin.dal.dataobject.permission.RoleDO;
import com.nexus.backend.admin.dal.dataobject.user.UserDO;
import com.nexus.backend.admin.service.permission.MenuService;
import com.nexus.backend.admin.service.permission.PermissionService;
import com.nexus.backend.admin.service.permission.RoleService;
import com.nexus.backend.admin.service.user.UserService;
import com.nexus.framework.security.service.DatabaseUserDetailsService;
import jakarta.annotation.Resource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DatabaseUserDetailsServiceImpl implements DatabaseUserDetailsService {

    @Resource
    private UserService userService;

    @Resource
    private PermissionService permissionService;

    @Override
    public UserDetails loadUserByUsername(String username) {
        UserDO user = userService.getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        Set<String> roleNames = permissionService.getUserAllPermissions(user.getId());
        Collection<GrantedAuthority> authorities = getAuthorities(roleNames);

        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(!user.isAccountNonExpired())
                .accountLocked(!user.isAccountNonLocked())
                .credentialsExpired(!user.isCredentialsNonExpired())
                .disabled(!user.isEnabled())
                .build();
    }

    private Collection<GrantedAuthority> getAuthorities(Set<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return Set.of();
        }

        return permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }
}
