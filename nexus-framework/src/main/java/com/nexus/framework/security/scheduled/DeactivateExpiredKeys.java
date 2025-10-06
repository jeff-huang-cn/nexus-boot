package com.nexus.framework.security.scheduled;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexus.framework.security.dal.dataobject.JwkDO;
import com.nexus.framework.security.dal.mapper.JwkMapper;
import com.nexus.framework.security.service.JwkService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class DeactivateExpiredKeys {

    @Resource
    private JwkService jwkService;

    @Resource
    private JwkMapper jwkMapper;

    /**
     * 每天凌晨2点禁用过期的JWK（设置 isActive=false）
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void deactivateExpiredKeys() {
        log.info("开始执行定时任务：禁用过期JWK");
        jwkService.deactivateExpiredKeys();
    }

    /**
     * 每天凌晨3点删除30天前过期的JWK（物理删除）
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void deleteExpiredKeys() {
        log.info("开始执行定时任务：删除30天前过期的JWK");
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        int deleted = jwkMapper.delete(new LambdaQueryWrapper<JwkDO>()
                .lt(JwkDO::getExpiresAt, thirtyDaysAgo));

        log.info("✅ 已删除{}个30天前过期的JWK", deleted);
    }
}
