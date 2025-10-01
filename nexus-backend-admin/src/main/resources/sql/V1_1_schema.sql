-- 代码生成管理平台数据库初始化脚本
-- 数据库：codegen_admin

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS codegen_admin CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE codegen_admin;

-- =============================================
-- 代码生成表配置
-- =============================================
DROP TABLE IF EXISTS `codegen_table`;
CREATE TABLE `codegen_table` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_name` varchar(200) NOT NULL DEFAULT '' COMMENT '表名称',
  `table_comment` varchar(500) NOT NULL DEFAULT '' COMMENT '表描述',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `module_name` varchar(30) NOT NULL COMMENT '模块名',
  `business_name` varchar(30) NOT NULL COMMENT '业务名',
  `class_name` varchar(100) NOT NULL DEFAULT '' COMMENT '类名称',
  `class_comment` varchar(50) NOT NULL COMMENT '类描述',
  `author` varchar(50) NOT NULL COMMENT '作者',
  `template_type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '模板类型：1-CRUD 2-树表 3-主子表',
  `front_type` tinyint(4) NOT NULL DEFAULT '20' COMMENT '前端类型：10-Vue2 20-Vue3 21-Vue3 Schema 30-React',
  `scene` tinyint(4) NOT NULL DEFAULT '1' COMMENT '场景：1-管理后台 2-用户APP',
  `package_name` varchar(100) NOT NULL COMMENT '生成包路径',
  `module_package_name` varchar(100) NOT NULL COMMENT '生成模块包路径',
  `business_package_name` varchar(100) NOT NULL COMMENT '生成业务包路径',
  `class_prefix` varchar(30) NOT NULL COMMENT '类名称前缀',
  `master_table_id` bigint(20) DEFAULT NULL COMMENT '主表编号（主子表专用）',
  `sub_join_column_name` varchar(30) DEFAULT NULL COMMENT '子表关联主表的字段名（主子表专用）',
  `sub_join_many` bit(1) DEFAULT NULL COMMENT '主表与子表是否一对多（主子表专用）',
  `tree_parent_column_name` varchar(30) DEFAULT NULL COMMENT '树表的父字段名（树表专用）',
  `tree_name_column_name` varchar(30) DEFAULT NULL COMMENT '树表的名字段名（树表专用）',
  `datasource_config_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '数据源配置编号',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_table_name` (`table_name`),
  KEY `idx_datasource_config_id` (`datasource_config_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代码生成表定义';

-- =============================================
-- 代码生成字段配置
-- =============================================
DROP TABLE IF EXISTS `codegen_column`;
CREATE TABLE `codegen_column` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_id` bigint(20) NOT NULL COMMENT '表编号',
  `column_name` varchar(200) NOT NULL COMMENT '字段名',
  `data_type` varchar(100) NOT NULL COMMENT '字段类型',
  `column_comment` varchar(500) NOT NULL COMMENT '字段描述',
  `nullable` bit(1) NOT NULL COMMENT '是否允许为空',
  `primary_key` bit(1) NOT NULL COMMENT '是否主键',
  `auto_increment` varchar(20) DEFAULT NULL COMMENT '是否自增',
  `ordinal_position` int(11) NOT NULL COMMENT '排序',
  `java_type` varchar(32) NOT NULL COMMENT 'Java 属性类型',
  `java_field` varchar(64) NOT NULL COMMENT 'Java 属性名',
  `dict_type` varchar(200) DEFAULT '' COMMENT '字典类型',
  `example` varchar(64) DEFAULT NULL COMMENT '数据示例',
  `create_operation` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否为Create创建操作的字段',
  `update_operation` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否为Update更新操作的字段',
  `list_operation` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否为List查询操作的字段',
  `list_operation_condition` varchar(32) NOT NULL DEFAULT '=' COMMENT 'List查询操作的条件类型',
  `list_operation_result` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否为List查询操作的返回字段',
  `html_type` varchar(32) NOT NULL COMMENT '显示类型',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_table_id` (`table_id`),
  KEY `idx_column_name` (`column_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代码生成表字段定义';

-- =============================================
-- 数据源配置表（支持多数据源）
-- =============================================
DROP TABLE IF EXISTS `datasource_config`;
CREATE TABLE `datasource_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键编号',
  `name` varchar(100) NOT NULL COMMENT '数据源名称',
  `url` varchar(1024) NOT NULL COMMENT '数据源连接',
  `username` varchar(255) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源配置表';

-- =============================================
-- 初始化数据源配置（默认本地数据源）
-- =============================================
INSERT INTO `datasource_config` (`id`, `name`, `url`, `username`, `password`, `creator`) VALUES 
(1, '本地数据源', 'jdbc:mysql://localhost:3306/codegen_admin?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=GMT%2B8', 'root', '123456', 'system');

-- =============================================
-- 创建索引
-- =============================================
-- codegen_table表索引
ALTER TABLE `codegen_table` ADD INDEX `idx_module_name` (`module_name`);
ALTER TABLE `codegen_table` ADD INDEX `idx_business_name` (`business_name`);
ALTER TABLE `codegen_table` ADD INDEX `idx_create_time` (`create_time`);

-- codegen_column表索引
ALTER TABLE `codegen_column` ADD INDEX `idx_java_field` (`java_field`);
ALTER TABLE `codegen_column` ADD INDEX `idx_html_type` (`html_type`);

-- 外键约束
ALTER TABLE `codegen_column` ADD CONSTRAINT `fk_codegen_column_table_id` FOREIGN KEY (`table_id`) REFERENCES `codegen_table` (`id`) ON DELETE CASCADE;
ALTER TABLE `codegen_table` ADD CONSTRAINT `fk_codegen_table_datasource_id` FOREIGN KEY (`datasource_config_id`) REFERENCES `datasource_config` (`id`);
