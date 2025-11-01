-- ============================================================
-- 字典管理系统初始化脚本（单表设计）
-- ============================================================

-- ----------------------------
-- 系统字典表（单表设计）
-- ----------------------------
DROP TABLE IF EXISTS `system_dict`;
CREATE TABLE `system_dict` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `dict_type` VARCHAR(100) NOT NULL COMMENT '字典类型（如：sys_user_sex、sys_user_status）',
  `dict_label` VARCHAR(100) NOT NULL COMMENT '字典标签（显示值）',
  `dict_value` VARCHAR(100) NOT NULL COMMENT '字典值（实际值）',
  `sort` INT DEFAULT 0 COMMENT '显示顺序',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `color_type` VARCHAR(20) DEFAULT NULL COMMENT '颜色类型（用于前端标签展示）',
  `css_class` VARCHAR(100) DEFAULT NULL COMMENT 'CSS类名',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `creator` VARCHAR(64) DEFAULT NULL COMMENT '创建者',
  `date_created` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(64) DEFAULT NULL COMMENT '更新者',
  `last_updated` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_dict_type` (`dict_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统字典表';

-- ----------------------------
-- 初始化字典数据（ID自动递增）
-- ----------------------------

-- 1. 用户性别
INSERT INTO `system_dict` (`dict_type`, `dict_label`, `dict_value`, `sort`, `color_type`, `remark`, `creator`) VALUES
('sys_user_sex', '未知', '0', 1, 'default', '性别未知', 'system'),
('sys_user_sex', '男', '1', 2, 'blue', '性别男', 'system'),
('sys_user_sex', '女', '2', 3, 'pink', '性别女', 'system');

-- 3. 菜单类型
INSERT INTO `system_dict` (`dict_type`, `dict_label`, `dict_value`, `sort`, `color_type`, `remark`, `creator`) VALUES
('sys_menu_type', '目录', '1', 1, 'primary', '菜单目录', 'system'),
('sys_menu_type', '菜单', '2', 2, 'success', '菜单项', 'system'),
('sys_menu_type', '按钮', '3', 3, 'warning', '按钮权限', 'system');

-- 4. 菜单状态
INSERT INTO `system_dict` (`dict_type`, `dict_label`, `dict_value`, `sort`, `color_type`, `remark`, `creator`) VALUES
('sys_menu_status', '禁用', '0', 1, 'danger', '菜单禁用', 'system'),
('sys_menu_status', '启用', '1', 2, 'success', '菜单启用', 'system');

-- 5. 角色状态
INSERT INTO `system_dict` (`dict_type`, `dict_label`, `dict_value`, `sort`, `color_type`, `remark`, `creator`) VALUES
('sys_role_status', '禁用', '0', 1, 'danger', '角色禁用', 'system'),
('sys_role_status', '启用', '1', 2, 'success', '角色启用', 'system');

-- 6. 通用状态
INSERT INTO `system_dict` (`dict_type`, `dict_label`, `dict_value`, `sort`, `color_type`, `remark`, `creator`) VALUES
('sys_common_status', '禁用', '0', 1, 'danger', '通用状态禁用', 'system'),
('sys_common_status', '启用', '1', 2, 'success', '通用状态启用', 'system');

-- 7. 是否选项
INSERT INTO `system_dict` (`dict_type`, `dict_label`, `dict_value`, `sort`, `color_type`, `remark`, `creator`) VALUES
('sys_yes_no', '否', '0', 1, 'default', '否', 'system'),
('sys_yes_no', '是', '1', 2, 'primary', '是', 'system');

