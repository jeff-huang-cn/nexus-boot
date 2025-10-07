-- =============================================
-- 修复缺失字段的补丁脚本
-- 如果表已经创建，执行此脚本添加缺失的字段
-- =============================================

-- 1. 修复 codegen_table 表缺失的字段
ALTER TABLE `codegen_table` 
  ADD COLUMN `class_prefix` varchar(30) DEFAULT '' COMMENT '类名前缀' AFTER `business_package_name`,
  ADD COLUMN `master_table_id` bigint(20) DEFAULT NULL COMMENT '主表ID（主子表专用）' AFTER `parent_menu_id`,
  ADD COLUMN `sub_join_column_name` varchar(64) DEFAULT NULL COMMENT '子表关联字段名（主子表专用）' AFTER `master_table_id`,
  ADD COLUMN `sub_join_many` bit(1) DEFAULT NULL COMMENT '是否一对多（主子表专用）' AFTER `sub_join_column_name`;

-- 说明：如果上述字段已存在会报错，属于正常情况，可以忽略


ALTER TABLE `system_menu`
    ADD COLUMN `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否 1-是' AFTER `always_show`,
    ADD INDEX `idx_deleted` (`deleted`);
