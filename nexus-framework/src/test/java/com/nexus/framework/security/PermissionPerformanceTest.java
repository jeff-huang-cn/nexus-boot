package com.nexus.framework.security;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

/**
 * 权限校验性能测试
 * 
 * 测试日志输出对性能的实际影响
 */
@Slf4j
public class PermissionPerformanceTest {

    private static final int ITERATIONS = 100_000;
    private static final Collection<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("system:menu:query"),
            new SimpleGrantedAuthority("system:menu:create"),
            new SimpleGrantedAuthority("system:menu:update"),
            new SimpleGrantedAuthority("system:menu:delete"));

    @Test
    public void testPerformanceComparison() {
        System.out.println("\n========== 权限校验性能测试 ==========\n");

        // 预热 JVM
        for (int i = 0; i < 10000; i++) {
            checkPermissionWithoutLog("system:menu:create");
            checkPermissionWithLog("system:menu:create");
        }

        // 测试1: 无日志版本
        long start1 = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            checkPermissionWithoutLog("system:menu:create");
        }
        long duration1 = System.nanoTime() - start1;

        // 测试2: 带日志版本（使用 isDebugEnabled 优化）
        long start2 = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            checkPermissionWithLogOptimized("system:menu:create");
        }
        long duration2 = System.nanoTime() - start2;

        // 测试3: 带日志版本（未优化）
        long start3 = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            checkPermissionWithLog("system:menu:create");
        }
        long duration3 = System.nanoTime() - start3;

        // 输出结果
        System.out.printf("测试次数: %,d 次\n\n", ITERATIONS);
        System.out.printf("1. 无日志版本:          总耗时 %,d ms, 平均 %.3f μs\n",
                duration1 / 1_000_000, duration1 / (double) ITERATIONS / 1000);
        System.out.printf("2. 带日志版本(优化):     总耗时 %,d ms, 平均 %.3f μs (慢 %.1f%%)\n",
                duration2 / 1_000_000, duration2 / (double) ITERATIONS / 1000,
                (duration2 - duration1) * 100.0 / duration1);
        System.out.printf("3. 带日志版本(未优化):   总耗时 %,d ms, 平均 %.3f μs (慢 %.1f%%)\n\n",
                duration3 / 1_000_000, duration3 / (double) ITERATIONS / 1000,
                (duration3 - duration1) * 100.0 / duration1);

        // 高并发模拟
        int qps = 1000;
        System.out.printf("========== 高并发场景模拟 (QPS = %,d) ==========\n\n", qps);
        System.out.printf("每秒权限校验开销:\n");
        System.out.printf("  无日志版本:        %.2f ms/秒\n",
                duration1 / (double) ITERATIONS / 1_000_000 * qps);
        System.out.printf("  带日志版本(优化):  %.2f ms/秒 (增加 %.2f ms)\n",
                duration2 / (double) ITERATIONS / 1_000_000 * qps,
                (duration2 - duration1) / (double) ITERATIONS / 1_000_000 * qps);
        System.out.printf("  带日志版本(未优化): %.2f ms/秒 (增加 %.2f ms)\n\n",
                duration3 / (double) ITERATIONS / 1_000_000 * qps,
                (duration3 - duration1) / (double) ITERATIONS / 1_000_000 * qps);
    }

    /**
     * 无日志版本（模拟原生 hasAuthority）
     */
    private boolean checkPermissionWithoutLog(String permission) {
        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 带日志版本（优化：使用 isDebugEnabled）
     */
    private boolean checkPermissionWithLogOptimized(String permission) {
        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals(permission)) {
                // 先判断日志级别，避免不必要的字符串拼接
                if (log.isDebugEnabled()) {
                    log.debug("权限校验通过：拥有权限 [{}]", permission);
                }
                return true;
            }
        }
        if (log.isWarnEnabled()) {
            log.warn("权限校验失败：缺少权限 [{}]", permission);
        }
        return false;
    }

    /**
     * 带日志版本（未优化：直接调用 log.debug）
     */
    private boolean checkPermissionWithLog(String permission) {
        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals(permission)) {
                // 未优化：即使 DEBUG 级别关闭，也会进行字符串拼接
                log.debug("权限校验通过：拥有权限 [{}]", permission);
                return true;
            }
        }
        log.warn("权限校验失败：缺少权限 [{}]", permission);
        return false;
    }
}
