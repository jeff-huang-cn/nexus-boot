package com.nexus.framework.security.task;

import com.nexus.framework.security.service.JwkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwkRotationTask {

    private final JwkService jwkService;

    /**
     * 每天凌晨2点检查是否需要轮换密钥
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void checkAndRotateKey() {
        try {
            log.info("开始检查JWK密钥是否需要轮换");

            if (jwkService.needsRotation()) {
                log.info("检测到密钥即将过期，开始自动轮换");
                jwkService.rotateKey();
                log.info("JWK密钥自动轮换成功");
            } else {
                log.debug("JWK密钥状态正常，无需轮换");
            }

            jwkService.deactivateExpiredKeys();

        } catch (Exception e) {
            log.error("JWK密钥轮换检查失败", e);
        }
    }
}
