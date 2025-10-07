package com.nexus.framework.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 自定义权限拒绝处理器
 * 
 * 当用户访问没有权限的资源时，记录日志并返回错误信息
 * 
 * @author nexus
 */
@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {

        // 获取当前用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "匿名用户";

        // 获取请求信息
        String method = request.getMethod();
        String uri = request.getRequestURI();

        // 输出权限拒绝日志
        if (log.isWarnEnabled()) {
            log.warn("❌ 权限拒绝：用户 [{}] 访问 [{} {}] 被拒绝，原因：{}",
                    username, method, uri, accessDeniedException.getMessage());

            // 输出用户当前拥有的权限
            if (authentication != null && authentication.getAuthorities() != null) {
                log.warn("   当前用户拥有的权限：{}", authentication.getAuthorities());
            }
        }

        // 返回 JSON 错误响应
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":403,\"message\":\"权限不足\",\"data\":null}");
    }
}
