package com.nexus.framework.security.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface DatabaseUserDetailsService {
    default UserDetails loadUserByUsername(String username) {
        return null;
    }
}
