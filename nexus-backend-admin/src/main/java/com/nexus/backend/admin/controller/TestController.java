package com.nexus.backend.admin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 *
 * @author beckend
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "OK");
        result.put("timestamp", System.currentTimeMillis());
        result.put("message", "服务正常运行");
        result.put("package", "com.beckend.admin");
        return result;
    }

}
