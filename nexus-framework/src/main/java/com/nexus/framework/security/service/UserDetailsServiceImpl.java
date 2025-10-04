package com.nexus.framework.security.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    @Resource
    private DatabaseUserDetailsService dataBaseUserDetailsService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("=== UserDetailsService加载用户 ===");
        log.info("用户名: {}", username);
        UserDetails userDetails = dataBaseUserDetailsService.loadUserByUsername(username);
        log.info("加载成功: {}", userDetails != null);
        return userDetails;
    }
}
