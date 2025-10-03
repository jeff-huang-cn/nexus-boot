-- ============================================================
-- 检查和修复菜单按钮权限数据
-- ============================================================

-- 1. 检查当前菜单数据统计
SELECT 
    CASE type 
        WHEN 1 THEN '目录'
        WHEN 2 THEN '菜单'
        WHEN 3 THEN '按钮'
    END as type_name,
    COUNT(*) as count
FROM system_menu
GROUP BY type
ORDER BY type;

-- 预期结果：
-- 目录: 2条 (系统管理、研发工具)
-- 菜单: 4条 (用户、角色、菜单、代码生成)
-- 按钮: 19条 (各种操作按钮)

-- 2. 查看具体的菜单树结构
SELECT 
    CONCAT(
        CASE type 
            WHEN 1 THEN '📁 '
            WHEN 2 THEN '  📄 '
            WHEN 3 THEN '    ├─ '
        END,
        name
    ) as 菜单名称,
    id,
    type,
    parent_id,
    permission
FROM system_menu
ORDER BY parent_id, sort;

-- 3. 如果没有按钮数据，检查是否缺少按钮
SELECT '缺少按钮数据！请执行 V20251003_4__init_all_menus.sql' as 提示
WHERE (SELECT COUNT(*) FROM system_menu WHERE type = 3) = 0;

-- 4. 验证用户管理菜单的按钮
SELECT * FROM system_menu 
WHERE parent_id = (SELECT id FROM system_menu WHERE path = '/system/user' LIMIT 1)
  AND type = 3;

-- 应该返回 4 条记录（查询、新增、编辑、删除）

-- 5. 如果确实缺少按钮，快速添加（仅用户管理示例）
SET @user_menu_id = (SELECT id FROM system_menu WHERE path = '/system/user' LIMIT 1);

INSERT IGNORE INTO system_menu (name, permission, type, sort, parent_id, status, visible, keep_alive, always_show, creator)
VALUES 
  ('查询', 'system:user:query', 3, 1, @user_menu_id, 1, 1, 0, 0, 'system'),
  ('新增', 'system:user:create', 3, 2, @user_menu_id, 1, 1, 0, 0, 'system'),
  ('编辑', 'system:user:update', 3, 3, @user_menu_id, 1, 1, 0, 0, 'system'),
  ('删除', 'system:user:delete', 3, 4, @user_menu_id, 1, 1, 0, 0, 'system');

-- 6. 为超级管理员分配新增的按钮权限
SET @admin_role_id = (SELECT id FROM system_role WHERE code = 'admin' LIMIT 1);

INSERT IGNORE INTO system_role_menu (role_id, menu_id, creator)
SELECT @admin_role_id, id, 'system'
FROM system_menu
WHERE type = 3;

-- 7. 验证修复结果
SELECT 
    m.name as 菜单名称,
    m.type as 类型,
    m.permission as 权限标识,
    pm.name as 父菜单
FROM system_menu m
LEFT JOIN system_menu pm ON m.parent_id = pm.id
WHERE m.path = '/system/user' OR m.parent_id = (SELECT id FROM system_menu WHERE path = '/system/user')
ORDER BY m.type, m.sort;

