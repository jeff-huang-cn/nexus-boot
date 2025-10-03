# 菜单显示问题修复指南

## 🐛 问题描述

**修复前的问题**：
1. ❌ 侧边栏没有"系统管理"目录
2. ❌ 用户管理、角色管理、菜单管理都有箭头（可展开）
3. ❌ 点击菜单项会展开，而不是跳转到页面

**期望效果**：
1. ✅ 有"系统管理"目录（可展开）
2. ✅ 用户管理、角色管理、菜单管理是菜单类型（无箭头）
3. ✅ 点击菜单项直接跳转到对应页面

---

## 🔧 修复内容

### 1. 后端修复 - MenuController.java

**文件**：`nexus-boot/nexus-backend-admin/src/main/java/com/nexus/backend/admin/controller/permission/MenuController.java`

**修改**：在 `getMenuTree()` 方法中过滤掉按钮权限（type=3）

```java
// 2. Controller 转换为 VO，过滤掉按钮类型（type=3）
// 按钮权限用于权限控制，不显示在菜单树中
List<MenuRespVO> menuVOList = menuList.stream()
    .filter(menu -> menu.getType() != 3) // 过滤掉按钮
    .map(menu -> BeanUtil.copyProperties(menu, MenuRespVO.class))
    .collect(Collectors.toList());
```

**原因**：按钮权限（如"查询"、"新增"、"编辑"、"删除"）是菜单的子节点，导致菜单项显示箭头。

---

### 2. 前端修复 - AppLayout.tsx

**文件**：`nexus-boot/nexus-backend-admin/ui/src/components/Layout/AppLayout.tsx`

**修改**：只对目录类型（type=1）递归处理子菜单

```typescript
// 只有目录类型（type=1）才递归处理子菜单
// 菜单类型（type=2）点击后直接跳转，不显示子菜单
children: item.type === 1 && item.children && item.children.length > 0
  ? convertMenus(item.children)
  : undefined,
```

**原因**：确保即使菜单类型有 children（已被后端过滤），也不会在前端显示展开箭头。

---

### 3. SQL 脚本确认

**文件**：`nexus-boot/nexus-backend-admin/src/main/resources/sql/V20251003_2__init_permission.sql`

**确认**：已创建"系统管理"目录

```sql
-- 2. 初始化系统管理目录（其他菜单由V20251003_4统一初始化）
INSERT INTO `system_menu` (`name`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `creator`) 
VALUES ('系统管理', 1, 1, 0, '/system', 'setting', 'Layout', 'system');
```

**文件**：`nexus-boot/nexus-backend-admin/src/main/resources/sql/V20251003_4__init_all_menus.sql`

**确认**：用户管理、角色管理、菜单管理都是菜单类型（type=2）

```sql
-- 用户管理 (type=2)
INSERT INTO `system_menu` 
(`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, ...)
SELECT '用户管理', 'system:user:query', 2, 1, @system_dir_id, '/system/user', ...

-- 角色管理 (type=2)
INSERT INTO `system_menu` 
(`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, ...)
SELECT '角色管理', 'system:role:query', 2, 2, @system_dir_id, '/system/role', ...

-- 菜单管理 (type=2)
INSERT INTO `system_menu` 
(`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, ...)
SELECT '菜单管理', 'system:menu:query', 2, 3, @system_dir_id, '/system/menu', ...
```

---

## 🧪 测试步骤

### 步骤1：重新初始化数据库（如果之前已执行过）

如果之前已经执行过 SQL 脚本，数据库中可能已有数据。有两种方式：

**方式1：清空菜单表重新初始化**
```sql
-- 清空菜单相关表
TRUNCATE TABLE `system_role_menu`;
TRUNCATE TABLE `system_menu`;

-- 然后重新执行初始化脚本
-- V20251003_2__init_permission.sql （只执行菜单相关部分）
-- V20251003_4__init_all_menus.sql
```

**方式2：检查现有数据**
```sql
-- 检查系统管理目录是否存在
SELECT * FROM `system_menu` WHERE `path` = '/system' AND `type` = 1;

-- 检查用户管理菜单类型
SELECT id, name, type, parent_id, path FROM `system_menu` 
WHERE `path` IN ('/system/user', '/system/role', '/system/menu');

-- 应该显示：
-- type = 2 (菜单类型)
-- parent_id = (系统管理目录的ID)
```

如果类型不对，手动更新：
```sql
UPDATE `system_menu` SET `type` = 2 
WHERE `path` IN ('/system/user', '/system/role', '/system/menu');
```

---

### 步骤2：重启后端服务

```bash
# 在 IDEA 中：
# 1. 停止当前运行的后端服务
# 2. 重新运行 AdminApplication
```

或者使用 Maven：
```bash
cd nexus-boot/nexus-backend-admin
mvn spring-boot:run
```

---

### 步骤3：重新编译前端

```bash
cd nexus-boot/nexus-backend-admin/ui
npm run build
# 或者开发模式
npm start
```

---

### 步骤4：清除浏览器缓存

1. 打开浏览器开发者工具（F12）
2. 右键点击刷新按钮
3. 选择"清空缓存并硬性重新加载"

---

### 步骤5：验证效果

**预期效果**：

```
侧边栏菜单：
├─ 仪表盘 (无箭头)
├─ 系统管理 (有箭头，可展开) ✅
│   ├─ 用户管理 (无箭头) ✅
│   ├─ 角色管理 (无箭头) ✅
│   └─ 菜单管理 (无箭头) ✅
└─ 研发工具 (有箭头，可展开) ✅
    └─ 代码生成 (无箭头) ✅
```

**交互验证**：
1. ✅ 点击"系统管理" → 展开/收起子菜单，不跳转
2. ✅ 点击"用户管理" → 直接跳转到用户列表页面
3. ✅ 点击"角色管理" → 直接跳转到角色列表页面
4. ✅ 点击"菜单管理" → 直接跳转到菜单列表页面
5. ✅ 用户管理、角色管理、菜单管理没有展开箭头

---

## 🔍 问题排查

### 问题1：菜单还是有箭头

**排查步骤**：

1. **检查数据库数据**
```sql
-- 查看菜单类型
SELECT id, name, type, parent_id, path, component 
FROM `system_menu` 
WHERE `visible` = 1
ORDER BY parent_id, sort;
```

2. **检查后端返回数据**

在浏览器开发者工具中查看接口返回：
```
Network → /api/system/menu/tree → Response
```

确认返回的数据中：
- 用户管理、角色管理、菜单管理的 `type` 应该是 `2`
- 这些菜单项不应该有 `children` 数组（或 `children` 为空）

3. **检查前端代码**

在 `AppLayout.tsx` 的 `convertMenus` 函数中添加 `console.log`：
```typescript
const convertMenus = (menuList: MenuItem[]): MenuProps['items'] => {
  console.log('Converting menus:', menuList);
  return menuList
    .filter(item => item.visible === 1 && (item.type === 1 || item.type === 2))
    .map(item => {
      const result = {
        key: item.path || `menu-${item.id}`,
        icon: getIcon(item.icon),
        label: item.name,
        children: item.type === 1 && item.children && item.children.length > 0
          ? convertMenus(item.children)
          : undefined,
      };
      console.log('Converted item:', item.name, 'type:', item.type, 'has children:', !!result.children);
      return result;
    });
};
```

---

### 问题2：系统管理目录不显示

**排查步骤**：

1. 检查数据库
```sql
SELECT * FROM `system_menu` WHERE `path` = '/system' AND `type` = 1;
```

2. 检查是否有权限
```sql
-- 查看当前用户的角色
SELECT r.* FROM `system_role` r
JOIN `system_user_role` ur ON r.id = ur.role_id
WHERE ur.user_id = 1; -- 替换为实际用户ID

-- 查看角色的菜单权限
SELECT m.* FROM `system_menu` m
JOIN `system_role_menu` rm ON m.id = rm.menu_id
WHERE rm.role_id = (SELECT id FROM `system_role` WHERE code = 'admin');
```

---

## 📊 数据库修复脚本（如需要）

如果数据库中的菜单类型不对，执行以下脚本：

```sql
-- 1. 确保系统管理目录存在
INSERT IGNORE INTO `system_menu` 
(`name`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `creator`) 
VALUES ('系统管理', 1, 1, 0, '/system', 'setting', 'Layout', 'system');

-- 2. 更新用户、角色、菜单为菜单类型(type=2)
UPDATE `system_menu` SET `type` = 2 
WHERE `path` IN ('/system/user', '/system/role', '/system/menu');

-- 3. 确保父级关系正确
SET @system_dir_id = (SELECT id FROM `system_menu` WHERE `path` = '/system' AND `type` = 1);

UPDATE `system_menu` SET `parent_id` = @system_dir_id
WHERE `path` IN ('/system/user', '/system/role', '/system/menu');

-- 4. 验证结果
SELECT id, name, type, parent_id, path 
FROM `system_menu` 
WHERE `path` IN ('/system', '/system/user', '/system/role', '/system/menu')
ORDER BY id;
```

---

## ✅ 验证清单

- [ ] 数据库中"系统管理"目录存在（type=1）
- [ ] 数据库中用户、角色、菜单是菜单类型（type=2）
- [ ] 后端 `/api/system/menu/tree` 返回的数据中菜单项没有 children
- [ ] 前端菜单显示正确：
  - [ ] 系统管理有箭头
  - [ ] 用户管理无箭头
  - [ ] 角色管理无箭头
  - [ ] 菜单管理无箭头
- [ ] 点击用户管理跳转到列表页面
- [ ] 点击角色管理跳转到列表页面
- [ ] 点击菜单管理跳转到列表页面

---

## 📚 相关文档

- [菜单结构设计说明](MENU_STRUCTURE.md)
- [SQL脚本执行说明](../src/main/resources/sql/README.md)

---

**最后更新时间**：2025-10-03

