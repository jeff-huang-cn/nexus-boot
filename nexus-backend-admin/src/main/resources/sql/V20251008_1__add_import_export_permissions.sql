-- 为用户管理和角色管理添加导入导出权限
-- 生成时间: 2025-10-08

-- ----------------------------
-- 1. 用户管理 - 添加导入导出权限
-- ----------------------------

-- 获取用户管理菜单ID
SET @user_menu_id = (SELECT id FROM `system_menu` WHERE `path` = '/system/user' AND `type` = 2 LIMIT 1);

-- 添加导入权限
INSERT INTO `system_menu` 
(`name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `always_show`, `creator`) 
SELECT '导入', 'system:user:import', 3, 5, @user_menu_id, 1, 1, 0, 0, 'system'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'system:user:import' AND `type` = 3);

-- 添加导出权限
INSERT INTO `system_menu` 
(`name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `always_show`, `creator`) 
SELECT '导出', 'system:user:export', 3, 6, @user_menu_id, 1, 1, 0, 0, 'system'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'system:user:export' AND `type` = 3);

-- ----------------------------
-- 2. 角色管理 - 添加导入导出权限
-- ----------------------------

-- 获取角色管理菜单ID
SET @role_menu_id = (SELECT id FROM `system_menu` WHERE `path` = '/system/role' AND `type` = 2 LIMIT 1);

-- 添加导入权限
INSERT INTO `system_menu` 
(`name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `always_show`, `creator`) 
SELECT '导入', 'system:role:import', 3, 6, @role_menu_id, 1, 1, 0, 0, 'system'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'system:role:import' AND `type` = 3);

-- 添加导出权限
INSERT INTO `system_menu` 
(`name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `always_show`, `creator`) 
SELECT '导出', 'system:role:export', 3, 7, @role_menu_id, 1, 1, 0, 0, 'system'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'system:role:export' AND `type` = 3);

-- ----------------------------
-- 3. 为超级管理员角色分配新权限
-- ----------------------------

-- 获取新添加的权限菜单ID并分配给超级管理员角色(role_id=1)
INSERT INTO `system_role_menu` (`role_id`, `menu_id`)
SELECT 1, id 
FROM `system_menu` 
WHERE `permission` IN (
    'system:user:import', 'system:user:export',
    'system:role:import', 'system:role:export'
)
AND `deleted` = 0
AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` 
    WHERE `role_id` = 1 AND `menu_id` = `system_menu`.`id`
);

COMMIT;

