package com.nexus.framework.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT退出成功处理器
 * 
 * 返回JSON格式的退出成功响应，适用于前后端分离场景
 * 
 * @author nexus
 */
@Slf4j
@Component
public class JwtLogoutSuccessHandler implements LogoutSuccessHandler {

    private final ObjectMapper objectMapper;

    public JwtLogoutSuccessHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        String username = authentication != null ? authentication.getName() : "unknown";
        log.info("用户退出登录成功: {}", username);

        // 设置响应头
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpStatus.OK.value());

        // 构建响应体
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "退出登录成功");
        result.put("data", null);

        // 返回JSON响应
        objectMapper.writeValue(response.getWriter(), result);
    }
}
