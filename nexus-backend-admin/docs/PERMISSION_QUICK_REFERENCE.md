# 权限系统快速参考

## 🎯 核心规则（必读！）

### 菜单类型与 permission 字段的关系

```
✅ 目录（type=1）：permission = NULL  ← 无需权限标识
✅ 菜单（type=2）：permission = NULL  ← 无需权限标识
✅ 按钮（type=3）：permission = 必填  ← 必须有权限标识
```

---

## 📋 SQL 脚本示例

### ✅ 正确写法

```sql
-- 1. 创建目录（无 permission）
INSERT INTO `system_menu` 
(`name`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`) 
VALUES ('系统管理', 1, 1, 0, '/system', 'setting', 'Layout');

-- 2. 创建菜单（无 permission）
INSERT INTO `system_menu` 
(`name`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`) 
VALUES ('用户管理', 2, 1, 1, '/system/user', 'user', 'system/user/index');

-- 3. 创建按钮（有 permission）
INSERT INTO `system_menu` 
(`name`, `permission`, `type`, `sort`, `parent_id`) 
VALUES ('新增', 'system:user:create', 3, 1, 2);
```

### ❌ 错误写法

```sql
-- ❌ 菜单不应该有 permission
INSERT INTO `system_menu` 
(`name`, `permission`, `type`, ...) 
VALUES ('用户管理', 'system:user:query', 2, ...);
```

---

## 🔧 权限控制方式

### 菜单显示控制

**通过角色-菜单关联表控制**：

```sql
-- system_role_menu 表
-- 如果有这条记录，则该角色的用户可以看到该菜单
INSERT INTO `system_role_menu` (role_id, menu_id) 
VALUES (1, 2);  -- 角色1 可以看到 菜单2（用户管理）
```

### 按钮显示控制

**通过按钮的 permission 字段控制**：

```typescript
// 前端代码
<PermissionButton permission="system:user:create">
  <Button>新增</Button>
</PermissionButton>
```

---

## 🌲 完整示例

### 数据库数据

```sql
-- 1. 系统管理目录（id=1, type=1, permission=null）
INSERT INTO `system_menu` (`name`, `type`, `path`, `icon`) 
VALUES ('系统管理', 1, '/system', 'setting');

-- 2. 用户管理菜单（id=2, type=2, permission=null, parent_id=1）
INSERT INTO `system_menu` (`name`, `type`, `parent_id`, `path`, `icon`, `component`) 
VALUES ('用户管理', 2, 1, '/system/user', 'user', 'system/user/index');

-- 3. 查询按钮（id=3, type=3, permission='system:user:query', parent_id=2）
INSERT INTO `system_menu` (`name`, `permission`, `type`, `parent_id`) 
VALUES ('查询', 'system:user:query', 3, 2);

-- 4. 新增按钮（id=4, type=3, permission='system:user:create', parent_id=2）
INSERT INTO `system_menu` (`name`, `permission`, `type`, `parent_id`) 
VALUES ('新增', 'system:user:create', 3, 2);
```

### 角色权限分配

```sql
-- 角色1（超级管理员）：拥有所有菜单
INSERT INTO `system_role_menu` (role_id, menu_id) VALUES
(1, 1),  -- 系统管理目录
(1, 2),  -- 用户管理菜单
(1, 3),  -- 查询按钮
(1, 4);  -- 新增按钮

-- 角色2（只读用户）：只能查询
INSERT INTO `system_role_menu` (role_id, menu_id) VALUES
(2, 1),  -- 系统管理目录
(2, 2),  -- 用户管理菜单
(2, 3);  -- 查询按钮（没有新增按钮）
```

### 用户看到的菜单

**超级管理员（角色1）**：
```
系统管理 ▶
  └─ 用户管理
      页面内按钮：[查询] [新增]
```

**只读用户（角色2）**：
```
系统管理 ▶
  └─ 用户管理
      页面内按钮：[查询]（没有新增按钮）
```

---

## 💡 常见问题

### Q1: 为什么菜单没有 permission？

**A**: 菜单只是导航结构，通过角色-菜单关联（`system_role_menu`）控制显示。只有按钮才需要 permission 来控制具体操作权限。

### Q2: 如何控制用户能看到哪些菜单？

**A**: 在 `system_role_menu` 表中为该用户的角色分配菜单ID。如果角色有某个菜单ID，用户就能看到该菜单。

### Q3: 如何添加新的操作按钮？

**A**: 
```sql
-- 1. 在 system_menu 表中添加按钮记录
INSERT INTO `system_menu` 
(`name`, `permission`, `type`, `parent_id`) 
VALUES ('导出', 'system:user:export', 3, 2);

-- 2. 为角色分配该按钮权限
INSERT INTO `system_role_menu` (role_id, menu_id) 
VALUES (1, LAST_INSERT_ID());

-- 3. 前端使用
<PermissionButton permission="system:user:export">
  <Button>导出</Button>
</PermissionButton>
```

### Q4: 后端如何获取当前用户的菜单？

**A**:
```java
// 1. 获取用户ID
Long userId = SecurityUtils.getCurrentUserId();

// 2. 获取用户的角色ID
List<Long> roleIds = userRoleMapper.selectByUserId(userId);

// 3. 获取角色的菜单ID
List<Long> menuIds = roleMenuMapper.selectByRoleIds(roleIds);

// 4. 查询菜单详情
List<MenuDO> menus = menuMapper.selectBatchIds(menuIds);
```

---

## 🎨 Permission 命名规范

### 格式

```
模块:功能:操作
```

### 示例

| 模块 | 功能 | 操作 | Permission |
|-----|------|------|------------|
| system | user | query | `system:user:query` |
| system | user | create | `system:user:create` |
| system | user | update | `system:user:update` |
| system | user | delete | `system:user:delete` |
| system | role | assign-menu | `system:role:assign-menu` |
| codegen | table | generate | `codegen:table:generate` |

---

## ✅ 检查清单

在初始化菜单前，请检查：

- [ ] 所有目录（type=1）的 permission 字段为 NULL
- [ ] 所有菜单（type=2）的 permission 字段为 NULL
- [ ] 所有按钮（type=3）的 permission 字段已填写
- [ ] Permission 格式符合规范（模块:功能:操作）
- [ ] 父子关系正确（按钮的 parent_id 指向菜单）
- [ ] 已为超级管理员分配所有菜单权限

---

## 📚 扩展阅读

- [权限系统设计详解](PERMISSION_DESIGN.md)
- [菜单结构说明](MENU_STRUCTURE.md)
- [SQL脚本执行指南](../src/main/resources/sql/README.md)

---

**最后更新时间**：2025-10-03

