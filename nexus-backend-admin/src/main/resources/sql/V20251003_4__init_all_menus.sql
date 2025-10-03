-- ============================================================
-- 完整菜单权限初始化脚本（统一版本）
-- 适用场景：全新初始化或增量更新，可重复执行
-- 依赖：需先执行 V20251003_2__init_permission.sql（创建基础表和超级管理员角色）
-- 功能：初始化所有菜单（系统管理+研发工具）
-- 约定：
--   1. component 字段对应 pages/ 目录下的文件路径（不含.tsx）
--   2. 目录（type=1）和菜单（type=2）不设置 permission 字段
--   3. 只有按钮（type=3）才设置 permission 字段，用于前端权限控制
--   4. 用户通过角色-菜单关联（system_role_menu）来控制能看到哪些菜单和按钮
-- ============================================================

-- ============================================================
-- 第一部分：系统管理模块
-- ============================================================

-- 1. 初始化系统管理目录（如果不存在则创建）
INSERT INTO `system_menu` 
(`name`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `always_show`, `creator`)
SELECT '系统管理', 1, 1, 0, '/system', 'setting', 'Layout', 1, 1, 0, 0, 'system'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `path` = '/system' AND `type` = 1);

-- 获取系统管理目录ID
SET @system_dir_id = (SELECT id FROM `system_menu` WHERE `path` = '/system' AND `type` = 1 LIMIT 1);

-- ----------------------------
-- 2. 用户管理菜单（如果不存在则创建）
-- ----------------------------
INSERT INTO `system_menu` 
(`name`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`) 
SELECT '用户管理', 2, 1, @system_dir_id, '/system/user', 'user', 'system/user/index', 'SystemUser', 1, 1, 1, 0, 'system'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `path` = '/system/user' AND `type` = 2);

-- 获取用户管理菜单ID
SET @user_menu_id = (SELECT id FROM `system_menu` WHERE `path` = '/system/user' AND `type` = 2 LIMIT 1);

-- 2.1 用户管理按钮权限
INSERT INTO `system_menu` 
(`name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `always_show`, `creator`) 
SELECT '查询', 'system:user:query', 3, 1, @user_menu_id, 1, 1, 0, 0, 'system'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'system:user:query' AND `type` = 3);

INSERT INTO `system_menu` 
(`name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `always_show`, `creator`) 
SELECT '新增', 'system:user:create', 3, 2, @user_menu_id, 1, 1, 0, 0, 'system'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'system:user:create');

INSERT INTO `system_menu` 
(`name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `always_show`, `creator`) 
SELECT '编辑', 'system:user:update', 3, 3, @user_menu_id, 1, 1, 0, 0, 'system'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'system:user:update');

INSERT INTO `system_menu` 
(`name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `always_show`, `creator`) 
SELECT '删除', 'system:user:delete', 3, 4, @user_menu_id, 1, 1, 0, 0, 'system'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'system:user:delete');

-- ----------------------------
-- 3. 角色管理菜单（如果不存在则创建）
-- ----------------------------
INSERT INTO `system_menu` 
(`name`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`) 
SELECT '角色管理', 2, 2, @system_dir_id, '/system/role', 'team', 'system/role/index', 'SystemRole', 1, 1, 1, 0, 'system'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `path` = '/system/role' AND `type` = 2);

-- 获取角色管理菜单ID
SET @role_menu_id = (SELECT id FROM `system_menu` WHERE `path` = '/system/role' AND `type` = 2 LIMIT 1);

-- 3.1 角色管理按钮权限
INSERT INTO `system_menu` 
(`name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `always_show`, `creator`) 
SELECT '查询', 'system:role:query', 3, 1, @role_menu_id, 1, 1, 0, 0, 'system'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'system:role:query' AND `type` = 3);

INSERT INTO `system_menu` 
(`name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `always_show`, `creator`) 
SELECT '新增', 'system:role:create', 3, 2, @role_menu_id, 1, 1, 0, 0, 'system'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'system:role:create');

INSERT INTO `system_menu` 
(`name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `always_show`, `creator`) 
SELECT '编辑', 'system:role:update', 3, 3, @role_menu_id, 1, 1, 0, 0, 'system'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'system:role:update');

INSERT INTO `system_menu` 
(`name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `always_show`, `creator`) 
SELECT '删除', 'system:role:delete', 3, 4, @role_menu_id, 1, 1, 0, 0, 'system'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'system:role:delete');

INSERT INTO `system_menu` 
(`name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `always_show`, `creator`) 
SELECT '分配权限', 'system:role:assign-menu', 3, 5, @role_menu_id, 1, 1, 0, 0, 'system'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'system:role:assign-menu');

-- ----------------------------
-- 4. 菜单管理菜单（如果不存在则创建）
-- ----------------------------
INSERT INTO `system_menu` 
(`name`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`) 
SELECT '菜单管理', 2, 3, @system_dir_id, '/system/menu', 'menu', 'system/menu/index', 'SystemMenu', 1, 1, 1, 0, 'system'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `path` = '/system/menu' AND `type` = 2);

-- 获取菜单管理菜单ID
SET @menu_menu_id = (SELECT id FROM `system_menu` WHERE `path` = '/system/menu' AND `type` = 2 LIMIT 1);

-- 4.1 菜单管理按钮权限
INSERT INTO `system_menu` 
(`name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `always_show`, `creator`) 
SELECT '查询', 'system:menu:query', 3, 1, @menu_menu_id, 1, 1, 0, 0, 'system'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'system:menu:query' AND `type` = 3);

INSERT INTO `system_menu` 
(`name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `always_show`, `creator`) 
SELECT '新增', 'system:menu:create', 3, 2, @menu_menu_id, 1, 1, 0, 0, 'system'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'system:menu:create');

INSERT INTO `system_menu` 
(`name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `always_show`, `creator`) 
SELECT '编辑', 'system:menu:update', 3, 3, @menu_menu_id, 1, 1, 0, 0, 'system'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'system:menu:update');

INSERT INTO `system_menu` 
(`name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `always_show`, `creator`) 
SELECT '删除', 'system:menu:delete', 3, 4, @menu_menu_id, 1, 1, 0, 0, 'system'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'system:menu:delete');

-- ============================================================
-- 第二部分：研发工具模块
-- ============================================================

-- ----------------------------
-- 5. 研发工具目录（如果不存在则创建）
-- ----------------------------
INSERT INTO `system_menu` 
(`name`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `always_show`, `creator`) 
SELECT '研发工具', 1, 2, 0, '/dev', 'tool', 'Layout', 1, 1, 0, 0, 'system'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `path` = '/dev' AND `type` = 1);

-- 获取研发工具目录ID
SET @dev_dir_id = (SELECT id FROM `system_menu` WHERE `path` = '/dev' AND `type` = 1 LIMIT 1);

-- ----------------------------
-- 6. 代码生成菜单（如果不存在则创建）
-- ----------------------------
INSERT INTO `system_menu` 
(`name`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`) 
SELECT '代码生成', 2, 1, @dev_dir_id, '/dev/codegen', 'code', 'dev/codegen/index', 'CodegenTableList', 1, 1, 1, 0, 'system'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `path` = '/dev/codegen' AND `type` = 2);

-- 获取代码生成菜单ID
SET @codegen_menu_id = (SELECT id FROM `system_menu` WHERE `path` = '/dev/codegen' AND `type` = 2 LIMIT 1);

-- 6.1 代码生成按钮权限
INSERT INTO `system_menu` 
(`name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `always_show`, `creator`) 
SELECT '查询', 'codegen:table:query', 3, 1, @codegen_menu_id, 1, 1, 0, 0, 'system'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'codegen:table:query' AND `type` = 3);

INSERT INTO `system_menu` 
(`name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `always_show`, `creator`) 
SELECT '导入', 'codegen:table:import', 3, 2, @codegen_menu_id, 1, 1, 0, 0, 'system'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'codegen:table:import');

INSERT INTO `system_menu` 
(`name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `always_show`, `creator`) 
SELECT '编辑', 'codegen:table:update', 3, 3, @codegen_menu_id, 1, 1, 0, 0, 'system'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'codegen:table:update');

INSERT INTO `system_menu` 
(`name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `always_show`, `creator`) 
SELECT '删除', 'codegen:table:delete', 3, 4, @codegen_menu_id, 1, 1, 0, 0, 'system'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'codegen:table:delete');

INSERT INTO `system_menu` 
(`name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `always_show`, `creator`) 
SELECT '预览', 'codegen:table:preview', 3, 5, @codegen_menu_id, 1, 1, 0, 0, 'system'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'codegen:table:preview');

INSERT INTO `system_menu` 
(`name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `always_show`, `creator`) 
SELECT '生成', 'codegen:table:generate', 3, 6, @codegen_menu_id, 1, 1, 0, 0, 'system'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'codegen:table:generate');

-- ============================================================
-- 第三部分：为超级管理员分配所有权限
-- ============================================================

-- 获取超级管理员角色ID
SET @admin_role_id = (SELECT id FROM `system_role` WHERE code = 'admin' LIMIT 1);

-- 为超级管理员分配所有菜单权限（使用INSERT IGNORE避免重复）
INSERT IGNORE INTO `system_role_menu` (`role_id`, `menu_id`, `creator`) 
SELECT @admin_role_id, id, 'system' 
FROM `system_menu`
WHERE @admin_role_id IS NOT NULL;

