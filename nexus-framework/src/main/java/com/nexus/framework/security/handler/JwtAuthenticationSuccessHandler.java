package com.nexus.framework.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.framework.security.generator.JwtTokenGenerator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Component("successHandler")
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
        log.info("=== 登录成功 ===");
        log.info("用户: {}", authentication.getName());
        log.info("权限: {}", authentication.getAuthorities());
        // 1. 生成JWT令牌
        String token = jwtTokenGenerator.generateToken(authentication);

        // 2. 构建响应数据（匹配前端期望的格式）
        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", token);
        data.put("tokenType", "Bearer");
        data.put("expiresIn", 7200); // 2小时，单位：秒

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("code", 200);
        responseBody.put("message", "登录成功");
        responseBody.put("data", data);

        // 3. 设置响应头和内容
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpStatus.OK.value());

        // 4. 返回JSON响应
        objectMapper.writeValue(response.getWriter(), responseBody);
    }
}