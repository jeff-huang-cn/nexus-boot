package com.nexus.framework.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录失败处理器：返回错误信息
 */
@Slf4j
@Component("failureHandler")
public class JwtAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationFailureHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
            HttpServletResponse response,
            org.springframework.security.core.AuthenticationException exception) throws IOException {
        log.error("=== 登录失败 ===");
        log.error("异常: {}", exception.getMessage());
        // 构建错误响应
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("code", 401);
        responseBody.put("message", "用户名或密码错误");
        responseBody.put("data", null);

        // 设置响应头和内容
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        // 返回JSON响应
        objectMapper.writeValue(response.getWriter(), responseBody);
    }
}
