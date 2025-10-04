package com.nexus.framework.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.framework.security.generator.JwtTokenGenerator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 登录成功处理器：生成JWT并返回给前端
 */
@Component
public class JwtAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenGenerator jwtTokenGenerator;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationSuccessHandler(JwtTokenGenerator jwtTokenGenerator, ObjectMapper objectMapper) {
        this.jwtTokenGenerator = jwtTokenGenerator;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // 1. 生成JWT令牌
        String token = jwtTokenGenerator.generateToken(authentication);
        
        // 2. 构建响应数据（包含令牌、用户名、权限）
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("code", 200);
        responseBody.put("message", "登录成功");
        responseBody.put("token", token);
        responseBody.put("username", authentication.getName());
        responseBody.put("authorities", authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .collect(Collectors.toList())); // 前端可直接使用的权限列表
        
        // 3. 返回JSON响应
        response.setContentType("application/json");
        response.setStatus(HttpStatus.OK.value());
        objectMapper.writeValue(response.getWriter(), responseBody);
    }
}

/**
 * 登录失败处理器：返回错误信息
 */
@Component
class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    public CustomAuthenticationFailureHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        org.springframework.security.core.AuthenticationException exception) throws IOException {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("code", 401);
        responseBody.put("message", "登录失败：" + exception.getMessage());
        
        response.setContentType("application/json");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        objectMapper.writeValue(response.getWriter(), responseBody);
    }
}
