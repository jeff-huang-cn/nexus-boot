-- =============================================
-- 代码生成器完整数据库脚本
-- =============================================

-- ----------------------------
-- 1. 数据源配置表
-- ----------------------------
DROP TABLE IF EXISTS `datasource_config`;
CREATE TABLE `datasource_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) NOT NULL COMMENT '数据源名称',
  `url` varchar(500) NOT NULL COMMENT '数据库连接',
  `username` varchar(64) NOT NULL COMMENT '用户名',
  `password` varchar(255) DEFAULT NULL COMMENT '密码',
  `date_created` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_updated` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源配置表';

-- ----------------------------
-- 2. 代码生成表配置
-- ----------------------------
DROP TABLE IF EXISTS `codegen_table`;
CREATE TABLE `codegen_table` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `datasource_config_id` bigint(20) NOT NULL COMMENT '数据源配置的编号',
  `scene` tinyint(4) NOT NULL DEFAULT '1' COMMENT '生成场景：1=管理后台 2=用户APP',
  `table_name` varchar(200) NOT NULL DEFAULT '' COMMENT '表名称',
  `table_comment` varchar(500) NOT NULL DEFAULT '' COMMENT '表描述',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `module_name` varchar(30) NOT NULL COMMENT '模块名',
  `business_name` varchar(30) NOT NULL COMMENT '业务名',
  `class_name` varchar(100) NOT NULL DEFAULT '' COMMENT '类名称',
  `class_comment` varchar(50) NOT NULL COMMENT '类描述',
  `author` varchar(50) NOT NULL COMMENT '作者',
  `template_type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '模板类型：1-单表CRUD 2-树表CRUD 3-主子表CRUD',
  `front_type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '前端类型：1-React 2-Vue3',
  `package_name` varchar(100) NOT NULL COMMENT '生成包路径',
  `module_package_name` varchar(100) NOT NULL COMMENT '模块包名',
  `business_package_name` varchar(100) NOT NULL COMMENT '业务包名',
  `class_prefix` varchar(30) DEFAULT '' COMMENT '类名前缀',
  `parent_menu_id` bigint(20) DEFAULT NULL COMMENT '父菜单编号',
  `master_table_id` bigint(20) DEFAULT NULL COMMENT '主表ID（主子表专用）',
  `sub_join_column_name` varchar(64) DEFAULT NULL COMMENT '子表关联字段名（主子表专用）',
  `sub_join_many` bit(1) DEFAULT NULL COMMENT '是否一对多（主子表专用）',
  `tree_parent_column_id` bigint(20) DEFAULT NULL COMMENT '树表的父字段编号（树表专用）',
  `tree_parent_column_name` varchar(30) DEFAULT NULL COMMENT '树表的父字段名（树表专用）',
  `tree_name_column_id` bigint(20) DEFAULT NULL COMMENT '树表的名字段编号（树表专用）',
  `tree_name_column_name` varchar(30) DEFAULT NULL COMMENT '树表的名字段名（树表专用）',
  `date_created` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_updated` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_datasource_config_id`(`datasource_config_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代码生成表定义';

-- ----------------------------
-- 3. 代码生成字段配置
-- ----------------------------
DROP TABLE IF EXISTS `codegen_column`;
CREATE TABLE `codegen_column` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_id` bigint(20) NOT NULL COMMENT '表编号',
  `column_name` varchar(200) NOT NULL COMMENT '字段名',
  `data_type` varchar(100) NOT NULL COMMENT '字段类型',
  `column_comment` varchar(500) NOT NULL COMMENT '字段描述',
  `nullable` bit(1) NOT NULL COMMENT '是否允许为空',
  `primary_key` bit(1) NOT NULL COMMENT '是否主键',
  `auto_increment` varchar(10) DEFAULT NULL COMMENT '是否自增',
  `ordinal_position` int(11) NOT NULL COMMENT '排序',
  `java_type` varchar(32) NOT NULL COMMENT 'Java 属性类型',
  `java_field` varchar(64) NOT NULL COMMENT 'Java 属性名',
  `dict_type` varchar(200) DEFAULT '' COMMENT '字典类型',
  `example` varchar(64) DEFAULT NULL COMMENT '数据示例',
  `create_operation` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否新增字段',
  `update_operation` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否编辑字段',
  `list_operation` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否列表查询字段',
  `list_operation_condition` varchar(16) NOT NULL DEFAULT 'EQ' COMMENT '查询条件类型（EQ/LIKE/BETWEEN/GT/LT/IN等）',
  `list_operation_result` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否列表展示字段',
  `html_type` varchar(32) DEFAULT NULL COMMENT '显示类型',
  `column_type` varchar(200) DEFAULT NULL COMMENT '列类型（含长度）',
  `date_created` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_updated` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_table_id`(`table_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代码生成表字段定义';

-- ----------------------------
-- 4. 插入默认数据源配置
-- ----------------------------
INSERT INTO `datasource_config` (`id`, `name`, `url`, `username`, `password`) VALUES
(1, '本地MySQL', 'jdbc:mysql://localhost:3306/codegen_admin?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8', 'root', '123456');

-- ----------------------------
-- 5. 示例：system_user 表（用于测试代码生成）
-- ----------------------------
DROP TABLE IF EXISTS `system_user`;
CREATE TABLE `system_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(30) NOT NULL COMMENT '用户账号',
  `password` varchar(100) DEFAULT '' COMMENT '密码',
  `nickname` varchar(30) NOT NULL COMMENT '用户昵称',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `dept_id` bigint(20) DEFAULT NULL COMMENT '部门ID',
  `post_ids` varchar(255) DEFAULT NULL COMMENT '岗位编号数组',
  `email` varchar(50) DEFAULT '' COMMENT '用户邮箱',
  `mobile` varchar(11) DEFAULT '' COMMENT '手机号码',
  `sex` tinyint(4) DEFAULT '0' COMMENT '用户性别（0=未知 1=男 2=女）',
  `avatar` varchar(512) DEFAULT '' COMMENT '头像地址',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '帐号状态（0=正常 1=停用）',
  `login_ip` varchar(50) DEFAULT '' COMMENT '最后登录IP',
  `login_date` datetime DEFAULT NULL COMMENT '最后登录时间',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `date_created` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `last_updated` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `idx_username` (`username`,`last_updated`,`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- 插入测试数据
INSERT INTO `system_user` (`id`, `username`, `password`, `nickname`, `dept_id`, `email`, `mobile`, `sex`, `status`) VALUES
(1, 'admin', '$2a$10$mRMIYLDtRHlf6.9ipiqH1.Z.bh/R9dO9d5iHiGYPigi6r5KOoR2Wm', '超级管理员', 103, 'admin@nexus.com', '15888888888', 1, 0),
(2, 'test', '$2a$10$mRMIYLDtRHlf6.9ipiqH1.Z.bh/R9dO9d5iHiGYPigi6r5KOoR2Wm', '测试用户', 105, 'test@nexus.com', '15666666666', 1, 0);

