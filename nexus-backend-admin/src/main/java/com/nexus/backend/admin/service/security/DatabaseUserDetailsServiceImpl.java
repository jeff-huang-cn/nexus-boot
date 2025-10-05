package com.nexus.backend.admin.service.security;

import com.nexus.backend.admin.dal.dataobject.user.UserDO;
import com.nexus.backend.admin.service.permission.PermissionService;
import com.nexus.backend.admin.service.user.UserService;
import com.nexus.framework.security.model.LoginUser;
import com.nexus.framework.security.service.DatabaseUserDetailsService;
import jakarta.annotation.Resource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Component
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

        return new LoginUser(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                user.isAccountNonExpired(),
                user.isCredentialsNonExpired(),
                user.isAccountNonLocked(),
                authorities);
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
