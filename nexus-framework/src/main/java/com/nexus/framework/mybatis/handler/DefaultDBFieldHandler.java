package com.nexus.framework.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.nexus.framework.security.util.SecurityContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * MyBatis-Plus 自动填充处理器
 */
@Slf4j
@Component
public class DefaultDBFieldHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始 INSERT 自动填充...");

        if (Objects.isNull(metaObject)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        // 填充创建时间
        Object dateCreated = getFieldValByName("dateCreated", metaObject);
        if (Objects.isNull(dateCreated)) {
            setFieldValByName("dateCreated", now, metaObject);
            log.debug("自动填充 dateCreated: {}", now);
        } else {
            log.debug("dateCreated 已有值，跳过填充: {}", dateCreated);
        }

        // 填充更新时间
        Object lastUpdated = getFieldValByName("lastUpdated", metaObject);
        if (Objects.isNull(lastUpdated)) {
            setFieldValByName("lastUpdated", now, metaObject);
            log.debug("自动填充 lastUpdated: {}", now);
        } else {
            log.debug("lastUpdated 已有值，跳过填充: {}", lastUpdated);
        }

        // 填充创建人和更新人（如果已登录）
        String userId = SecurityContextUtils.getLoginUserIdAsString();
        if (Objects.nonNull(userId)) {
            // 填充创建人
            Object creator = getFieldValByName("creator", metaObject);
            if (Objects.isNull(creator)) {
                setFieldValByName("creator", userId, metaObject);
                log.debug("自动填充 creator: {}", userId);
            } else {
                log.debug("creator 已有值，跳过填充: {}", creator);
            }

            // 填充更新人
            Object updater = getFieldValByName("updater", metaObject);
            if (Objects.isNull(updater)) {
                setFieldValByName("updater", userId, metaObject);
                log.debug("自动填充 updater: {}", userId);
            } else {
                log.debug("updater 已有值，跳过填充: {}", updater);
            }
        } else {
            log.debug("未获取到当前登录用户ID，跳过 creator 和 updater 填充（可能未登录或在系统任务中）");
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始 UPDATE 自动填充...");

        // 填充更新时间
        Object lastUpdated = getFieldValByName("lastUpdated", metaObject);
        if (Objects.isNull(lastUpdated)) {
            LocalDateTime now = LocalDateTime.now();
            setFieldValByName("lastUpdated", now, metaObject);
            log.debug("自动填充 lastUpdated: {}", now);
        } else {
            log.debug("lastUpdated 已有值，跳过填充: {}", lastUpdated);
        }

        // 填充更新人（如果已登录）
        String userId = SecurityContextUtils.getLoginUserIdAsString();
        if (Objects.nonNull(userId)) {
            Object updater = getFieldValByName("updater", metaObject);
            if (Objects.isNull(updater)) {
                setFieldValByName("updater", userId, metaObject);
                log.debug("自动填充 updater: {}", userId);
            } else {
                log.debug("updater 已有值，跳过填充: {}", updater);
            }
        } else {
            log.debug("未获取到当前登录用户ID，跳过 updater 填充");
        }
    }
}
