-- ============================================================
-- 清理权限管理相关表数据
-- 警告：此脚本会删除所有权限相关数据，仅用于开发环境重新初始化
-- ============================================================

-- 1. 清理关联表数据（先删除外键关联）
DELETE FROM `system_user_role`;
DELETE FROM `system_role_menu`;

-- 2. 清理菜单数据
DELETE FROM `system_menu`;

-- 3. 清理角色数据
DELETE FROM `system_role`;

-- 4. 重置自增ID（可选，如果需要从1开始）
ALTER TABLE `system_menu` AUTO_INCREMENT = 1;
ALTER TABLE `system_role` AUTO_INCREMENT = 1;
ALTER TABLE `system_role_menu` AUTO_INCREMENT = 1;
ALTER TABLE `system_user_role` AUTO_INCREMENT = 1;

-- 执行完成后，可以重新按顺序执行初始化脚本：
-- 1. V20251003_2__init_permission.sql
-- 2. V20251003_3__init_dict.sql
-- 3. V20251003_4__init_menu_full.sql（或 V20251003_5__update_menu_codegen.sql）

