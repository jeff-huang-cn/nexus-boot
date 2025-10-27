-- 部门管理表模块菜单权限SQL
-- 生成时间: 2025-10-27 22:36:36

-- 创建父菜单（如果未指定父菜单，则创建一级菜单）
-- 使用指定的父菜单
SET @parent_menu_id = 1;

-- 获取该父菜单下类型为菜单(type=2)的最大sort值
SELECT IFNULL(MAX(sort), 0) + 1 INTO @menu_sort
FROM system_menu
WHERE parent_id = @parent_menu_id AND type = 2 AND deleted = 0;

-- 创建功能菜单
INSERT INTO system_menu
(parent_id, name, permission, type, sort, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, date_created, updater, last_updated, deleted)
VALUES
    (@parent_menu_id, '部门管理表', 'system:dept:query', 2, @menu_sort, '/dept', '', 'system/dept/index', 'Dept', 1, 1, 1, 0, 'admin', NOW(), 'admin', NOW(), 0);

SET @menu_id = LAST_INSERT_ID();

INSERT INTO system_menu
(parent_id, name, permission, type, sort, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, date_created, updater, last_updated, deleted)
VALUES
    (@menu_id, '查询部门管理表', 'system:dept:query', 3, 1, '', '', '', '', 1, 1, 0, 0, 'admin', NOW(), 'admin', NOW(), 0),
    (@menu_id, '新增部门管理表', 'system:dept:create', 3, 2, '', '', '', '', 1, 1, 0, 0, 'admin', NOW(), 'admin', NOW(), 0),
    (@menu_id, '修改部门管理表', 'system:dept:update', 3, 3, '', '', '', '', 1, 1, 0, 0, 'admin', NOW(), 'admin', NOW(), 0),
    (@menu_id, '删除部门管理表', 'system:dept:delete', 3, 4, '', '', '', '', 1, 1, 0, 0, 'admin', NOW(), 'admin', NOW(), 0),
    (@menu_id, '导出部门管理表', 'system:dept:export', 3, 5, '', '', '', '', 1, 1, 0, 0, 'admin', NOW(), 'admin', NOW(), 0);

INSERT INTO system_role_menu (role_id, menu_id)
SELECT 1, id FROM system_menu WHERE deleted = 0 AND permission LIKE 'system:dept:%';

COMMIT;

