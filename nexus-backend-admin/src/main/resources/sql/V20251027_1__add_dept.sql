-- ============================================================
-- 部门管理表初始化脚本
-- 功能：创建部门管理表结构，支持树形结构和多租户隔离
-- ============================================================

SET NAMES utf8mb4;

-- ----------------------------
-- 部门管理表
-- ----------------------------
DROP TABLE IF EXISTS `system_dept`;
CREATE TABLE `system_dept` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `name` VARCHAR(50) NOT NULL COMMENT '部门名称',
  `code` VARCHAR(50) DEFAULT NULL COMMENT '部门编码',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父部门ID（0表示根部门）',
  `sort` INT DEFAULT 0 COMMENT '显示顺序',
  `leader_user_id` BIGINT DEFAULT NULL COMMENT '负责人ID',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `email` VARCHAR(50) DEFAULT NULL COMMENT '邮箱',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
  `creator` VARCHAR(64) DEFAULT NULL COMMENT '创建者',
  `date_created` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(64) DEFAULT NULL COMMENT '更新者',
  `last_updated` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否 1-是',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_deleted` (`deleted`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门管理表';