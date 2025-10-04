package com.nexus.backend.admin.controller.auth;

import com.nexus.backend.admin.dal.dataobject.user.UserDO;
import com.nexus.backend.admin.service.permission.PermissionService;
import com.nexus.backend.admin.service.user.UserService;
import com.nexus.framework.security.generator.JwtTokenGenerator;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 认证控制器
 * 
 * 提供登录、退出登录等功能
 * 
 * @author nexus
 */
@Slf4j
//@RestController
//@RequestMapping("/auth")
public class AuthController {

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private JwtTokenGenerator jwtTokenGenerator;

    @Resource
    private UserService userService;

    @Resource
    private PermissionService permissionService;

    /**
     * 登录接口
     * 
     * @param loginRequest 登录请求
     * @return 包含JWT Token的响应
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) {
        log.info("=== 登录接口被调用 ===");
        log.info("用户名: {}", loginRequest.getUsername());

        try {
            // 1. 使用Spring Security的AuthenticationManager进行认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));

            log.info("认证成功，用户: {}", authentication.getName());
            log.info("权限数量: {}", authentication.getAuthorities().size());

            // 2. 查询用户信息
            UserDO user = userService.getUserByUsername(loginRequest.getUsername());
            if (user == null) {
                log.error("用户不存在: {}", loginRequest.getUsername());
                return buildErrorResponse(401, "用户不存在");
            }

            // 3. 查询用户的所有按钮权限
            Set<String> permissions = permissionService.getUserAllPermissions(user.getId());
            log.info("用户拥有 {} 个按钮权限", permissions.size());

            // 4. 生成JWT Token（Token中包含权限信息）
            String token = jwtTokenGenerator.generateToken(authentication);
            log.info("JWT Token生成成功");

            // 5. 构建响应
            Map<String, Object> response = buildSuccessResponse("登录成功");
            Map<String, Object> data = new HashMap<>();
            data.put("accessToken", token);
            data.put("tokenType", "Bearer");
            data.put("expiresIn", 7200); // 2小时
            data.put("userId", user.getId());
            data.put("username", user.getUsername());
            data.put("nickname", user.getNickname());
            // 将权限列表也返回（前端可以直接使用，无需解析JWT）
            data.put("permissions", permissions);
            // 权限字符串（空格分隔，与JWT中的authorities一致）
            data.put("authorities", authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));

            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            log.error("认证失败: {}", e.getMessage());
            return buildErrorResponse(401, "用户名或密码错误");
        } catch (Exception e) {
            log.error("登录失败", e);
            return buildErrorResponse(500, "登录失败: " + e.getMessage());
        }
    }

    /**
     * 退出登录接口
     * 
     * 前后端分离，JWT是无状态的，退出登录只需要前端清除Token即可
     * 如果需要黑名单机制，可以在这里将Token加入黑名单
     * 
     * @return 响应
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        log.info("=== 退出登录接口被调用 ===");

        // TODO: 如果需要Token黑名单机制，在这里实现
        // 1. 从请求头获取Token
        // 2. 将Token加入Redis黑名单，设置过期时间为Token的剩余有效期
        // 3. JWT验证时检查黑名单

        Map<String, Object> response = buildSuccessResponse("退出登录成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 获取当前登录用户信息
     * 
     * @return 用户信息
     */
    @GetMapping("/userinfo")
    public ResponseEntity<Map<String, Object>> getUserInfo(Authentication authentication) {
        log.info("=== 获取用户信息接口被调用 ===");

        if (authentication == null || !authentication.isAuthenticated()) {
            return buildErrorResponse(401, "未登录");
        }

        try {
            String username = authentication.getName();
            UserDO user = userService.getUserByUsername(username);

            if (user == null) {
                return buildErrorResponse(404, "用户不存在");
            }

            // 获取用户权限
            Set<String> permissions = permissionService.getUserAllPermissions(user.getId());

            Map<String, Object> response = buildSuccessResponse("查询成功");
            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getId());
            data.put("username", user.getUsername());
            data.put("nickname", user.getNickname());
            data.put("email", user.getEmail());
            data.put("mobile", user.getMobile());
            data.put("sex", user.getSex());
            data.put("avatar", user.getAvatar());
            data.put("status", user.getStatus());
            data.put("permissions", permissions);

            response.put("data", data);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return buildErrorResponse(500, "获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 刷新Token接口（可选）
     * 
     * @param refreshRequest 刷新请求
     * @return 新的Token
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(@RequestBody RefreshRequest refreshRequest) {
        log.info("=== 刷新Token接口被调用 ===");

        // TODO: 实现Token刷新逻辑
        // 1. 验证旧Token是否有效
        // 2. 从旧Token中提取用户信息
        // 3. 生成新Token
        // 4. （可选）将旧Token加入黑名单

        return buildErrorResponse(501, "Token刷新功能暂未实现");
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 构建成功响应
     */
    private Map<String, Object> buildSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", message);
        return response;
    }

    /**
     * 构建错误响应
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(int code, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", code);
        response.put("message", message);
        return ResponseEntity.status(code).body(response);
    }

    // ==================== DTO ====================

    /**
     * 登录请求DTO
     */
    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    /**
     * 刷新Token请求DTO
     */
    @Data
    public static class RefreshRequest {
        private String refreshToken;
    }
}
