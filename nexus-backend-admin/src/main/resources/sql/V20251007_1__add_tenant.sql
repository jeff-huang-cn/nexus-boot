SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `system_tenant` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '租户ID',
    `name` VARCHAR(50) NOT NULL COMMENT '租户名称',
    `code` VARCHAR(50) NOT NULL COMMENT '租户编码（用于识别租户）',
    `datasource_id` BIGINT NOT NULL COMMENT '数据源ID（关联datasource_config.id）',
    `menu_ids` JSON NULL COMMENT '分配的菜单ID集合，格式：[1, 2, 100, 101, 102]',
    `max_users` INT NOT NULL DEFAULT 10 COMMENT '用户数量（可创建的用户数量）',
    `expire_time` DATETIME NOT NULL COMMENT '过期时间',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `creator` VARCHAR(64) NULL DEFAULT NULL COMMENT '创建人',
    `date_created` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) NULL DEFAULT NULL COMMENT '更新人',
    `last_updated` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否 1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name_deleted` (`name`, `deleted`) COMMENT '租户名称唯一索引（含软删除）',
    UNIQUE KEY `uk_code_deleted` (`code`, `deleted`) COMMENT '租户编码唯一索引（含软删除）',
    KEY `idx_status` (`status`),
    KEY `idx_deleted` (`deleted`),
    KEY `idx_datasource_id` (`datasource_id`),
    KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户管理表';