-- 修复租户模块的权限配置
-- 将 tenant:tenant:* 修改为 system:tenant:*

-- 更新菜单权限
UPDATE system_menu 
SET permission = REPLACE(permission, 'tenant:tenant:', 'system:tenant:')
WHERE permission LIKE 'tenant:tenant:%';

-- 如果需要更新菜单的component路径（如果数据库中有的话）
-- UPDATE system_menu 
-- SET component = 'system/tenant/index'
-- WHERE component = 'tenant/page' OR component = 'tenant/tenant';

COMMIT;

