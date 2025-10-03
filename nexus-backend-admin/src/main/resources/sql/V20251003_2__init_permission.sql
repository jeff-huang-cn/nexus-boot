-- ============================================================
-- 权限管理系统初始化脚本（基础版）
-- 功能：创建权限管理基础表结构、初始化超级管理员角色和系统管理目录
-- 说明：菜单数据由 V20251003_4__init_all_menus.sql 统一初始化
-- ============================================================

-- ----------------------------
-- 1. 系统菜单表
-- ----------------------------
DROP TABLE IF EXISTS `system_menu`;
CREATE TABLE `system_menu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` VARCHAR(50) NOT NULL COMMENT '菜单名称',
  `permission` VARCHAR(100) DEFAULT NULL COMMENT '权限标识（如：system:user:create）',
  `type` TINYINT NOT NULL COMMENT '菜单类型：1-目录 2-菜单 3-按钮',
  `sort` INT DEFAULT 0 COMMENT '显示顺序',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父菜单ID（0表示根菜单）',
  `path` VARCHAR(200) DEFAULT NULL COMMENT '路由地址（前端路由路径）',
  `icon` VARCHAR(100) DEFAULT NULL COMMENT '菜单图标',
  `component` VARCHAR(255) DEFAULT NULL COMMENT '组件路径（对应pages/目录下的路径，不含.tsx）',
  `component_name` VARCHAR(50) DEFAULT NULL COMMENT '组件名称',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `visible` TINYINT DEFAULT 1 COMMENT '是否可见：0-隐藏 1-显示',
  `keep_alive` TINYINT DEFAULT 1 COMMENT '是否缓存：0-不缓存 1-缓存',
  `always_show` TINYINT DEFAULT 0 COMMENT '是否总是显示：0-否 1-是',
  `creator` VARCHAR(64) DEFAULT NULL COMMENT '创建者',
  `date_created` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(64) DEFAULT NULL COMMENT '更新者',
  `last_updated` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统菜单表';

-- ----------------------------
-- 2. 系统角色表
-- ----------------------------
DROP TABLE IF EXISTS `system_role`;
CREATE TABLE `system_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` VARCHAR(30) NOT NULL COMMENT '角色名称',
  `code` VARCHAR(100) NOT NULL COMMENT '角色编码（如：admin、user）',
  `sort` INT DEFAULT 0 COMMENT '显示顺序',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `type` TINYINT DEFAULT 2 COMMENT '角色类型：1-系统内置 2-自定义',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `creator` VARCHAR(64) DEFAULT NULL COMMENT '创建者',
  `date_created` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(64) DEFAULT NULL COMMENT '更新者',
  `last_updated` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- ----------------------------
-- 3. 角色菜单关联表
-- ----------------------------
DROP TABLE IF EXISTS `system_role_menu`;
CREATE TABLE `system_role_menu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
  `creator` VARCHAR(64) DEFAULT NULL COMMENT '创建者',
  `date_created` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- ----------------------------
-- 4. 用户角色关联表
-- ----------------------------
DROP TABLE IF EXISTS `system_user_role`;
CREATE TABLE `system_user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `creator` VARCHAR(64) DEFAULT NULL COMMENT '创建者',
  `date_created` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ----------------------------
-- 初始化数据
-- ----------------------------

-- 1. 初始化角色：超级管理员
INSERT INTO `system_role` (`name`, `code`, `sort`, `status`, `type`, `remark`, `creator`) 
VALUES ('超级管理员', 'admin', 0, 1, 1, '超级管理员拥有所有权限', 'system');

-- 获取刚创建的角色ID
SET @admin_role_id = LAST_INSERT_ID();

-- 2. 初始化系统管理目录（其他菜单由V20251003_4统一初始化）
INSERT INTO `system_menu` (`name`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `creator`) 
VALUES ('系统管理', 1, 1, 0, '/system', 'setting', 'Layout', 'system');

-- 3. 为默认用户分配超级管理员角色（假设ID为1的用户存在）
INSERT IGNORE INTO `system_user_role` (`user_id`, `role_id`, `creator`) 
VALUES (1, @admin_role_id, 'system');
