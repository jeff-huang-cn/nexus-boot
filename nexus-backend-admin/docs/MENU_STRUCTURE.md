# 菜单结构设计说明

## 📋 菜单类型定义

| 类型 | type值 | 名称 | 说明 | 前端表现 |
|-----|--------|------|------|---------|
| Directory | 1 | 目录 | 菜单分组，可以有子菜单 | 显示展开箭头，点击展开/收起 |
| Menu | 2 | 菜单 | 具体的功能页面 | 无箭头，点击跳转到对应页面 |
| Button | 3 | 按钮 | 页面内的操作权限 | 不在菜单中显示，用于权限控制 |

---

## 🌲 初始化菜单结构

### 完整层级结构

```
仪表盘 (固定)
  └─ /dashboard

系统管理 (目录, type=1)
  └─ /system
      ├─ 用户管理 (菜单, type=2)
      │   └─ /system/user
      │       ├─ 查询 (按钮, type=3)
      │       ├─ 新增 (按钮, type=3)
      │       ├─ 编辑 (按钮, type=3)
      │       └─ 删除 (按钮, type=3)
      │
      ├─ 角色管理 (菜单, type=2)
      │   └─ /system/role
      │       ├─ 查询 (按钮, type=3)
      │       ├─ 新增 (按钮, type=3)
      │       ├─ 编辑 (按钮, type=3)
      │       ├─ 删除 (按钮, type=3)
      │       └─ 分配权限 (按钮, type=3)
      │
      └─ 菜单管理 (菜单, type=2)
          └─ /system/menu
              ├─ 查询 (按钮, type=3)
              ├─ 新增 (按钮, type=3)
              ├─ 编辑 (按钮, type=3)
              └─ 删除 (按钮, type=3)

研发工具 (目录, type=1)
  └─ /dev
      └─ 代码生成 (菜单, type=2)
          └─ /dev/codegen
              ├─ 查询 (按钮, type=3)
              ├─ 导入 (按钮, type=3)
              ├─ 编辑 (按钮, type=3)
              ├─ 删除 (按钮, type=3)
              ├─ 预览 (按钮, type=3)
              └─ 生成 (按钮, type=3)
```

---

## 🔧 关键设计点

### 1. 目录 vs 菜单

**目录 (type=1)**：
- ✅ 用于菜单分组
- ✅ 可以有子菜单（目录或菜单）
- ✅ 前端显示展开箭头
- ✅ 点击展开/收起，不跳转页面
- ✅ 示例：系统管理、研发工具

**菜单 (type=2)**：
- ✅ 对应具体的功能页面
- ✅ 点击跳转到对应的 component 页面
- ✅ 前端不显示展开箭头（即使有 children）
- ✅ children 只包含按钮权限（type=3），不包含子菜单
- ✅ 示例：用户管理、角色管理、菜单管理

**按钮 (type=3)**：
- ✅ 页面内的操作权限
- ✅ 不在侧边菜单中显示
- ✅ 用于前端的按钮权限控制（usePermission Hook）
- ✅ 示例：查询、新增、编辑、删除

### 2. 菜单树过滤逻辑

**后端过滤** (`MenuController.getMenuTree()`):
```java
// 过滤掉按钮类型（type=3）
List<MenuRespVO> menuVOList = menuList.stream()
    .filter(menu -> menu.getType() != 3) // 只返回目录和菜单
    .map(menu -> BeanUtil.copyProperties(menu, MenuRespVO.class))
    .collect(Collectors.toList());
```

**前端过滤** (`AppLayout.convertMenus()`):
```typescript
// 只有目录类型（type=1）才递归处理子菜单
children: item.type === 1 && item.children && item.children.length > 0
  ? convertMenus(item.children)
  : undefined,
```

### 3. 权限标识规范

格式：`模块:功能:操作`

**示例**：
- `system:user:query` - 系统管理:用户管理:查询
- `system:user:create` - 系统管理:用户管理:新增
- `system:role:assign-menu` - 系统管理:角色管理:分配权限
- `codegen:table:generate` - 代码生成:表管理:生成代码

---

## 📂 Component 路径约定

**约定**：`component` 字段对应 `pages/` 目录下的文件路径（不含 `.tsx`）

| 菜单名称 | component | 实际文件路径 |
|---------|-----------|-------------|
| 系统管理 | `Layout` | 目录，无具体文件 |
| 用户管理 | `system/user/index` | `pages/system/user/index.tsx` |
| 角色管理 | `system/role/index` | `pages/system/role/index.tsx` |
| 菜单管理 | `system/menu/index` | `pages/system/menu/index.tsx` |
| 代码生成 | `dev/codegen/index` | `pages/dev/codegen/index.tsx` |

**前端动态加载**：
```typescript
// DynamicRoutes.tsx
const Component = React.lazy(() => 
  import(`../../pages/${menu.component}`)
);
```

---

## 📊 数据库字段说明

### system_menu 表核心字段

| 字段 | 类型 | 说明 | 示例 |
|-----|------|------|------|
| `name` | VARCHAR(50) | 菜单名称 | "用户管理" |
| `permission` | VARCHAR(100) | 权限标识 | "system:user:query" |
| `type` | TINYINT | 菜单类型 | 1-目录, 2-菜单, 3-按钮 |
| `sort` | INT | 显示顺序 | 1, 2, 3... |
| `parent_id` | BIGINT | 父菜单ID | 0表示根菜单 |
| `path` | VARCHAR(200) | 路由地址 | "/system/user" |
| `icon` | VARCHAR(100) | 菜单图标 | "user" (对应 UserOutlined) |
| `component` | VARCHAR(255) | 组件路径 | "system/user/index" |
| `component_name` | VARCHAR(50) | 组件名称 | "SystemUser" |
| `status` | TINYINT | 状态 | 0-禁用, 1-启用 |
| `visible` | TINYINT | 是否可见 | 0-隐藏, 1-显示 |
| `keep_alive` | TINYINT | 是否缓存 | 0-不缓存, 1-缓存 |
| `always_show` | TINYINT | 是否总是显示 | 0-否, 1-是 |

---

## 🎨 图标映射规则

**约定**：图标名称自动转换为 Ant Design Icons 的组件名

**转换规则**：
```
数据库存储值 → 图标组件名
------------------------
user       → UserOutlined
team       → TeamOutlined
menu       → MenuOutlined
setting    → SettingOutlined
code       → CodeOutlined
tool       → ToolOutlined
```

**前端实现** (`AppLayout.getIcon()`):
```typescript
// 'user' -> 'UserOutlined'
const iconComponentName = iconName.charAt(0).toUpperCase() + 
  iconName.slice(1).toLowerCase() + 'Outlined';
```

---

## 🚀 SQL 脚本执行顺序

1. **V20251003_2__init_permission.sql**
   - 创建权限管理基础表（`system_menu`, `system_role`, `system_role_menu`, `system_user_role`）
   - 初始化超级管理员角色
   - 创建"系统管理"目录

2. **V20251003_3__init_dict.sql**
   - 创建字典表（`system_dict`）
   - 初始化字典数据

3. **V20251003_4__init_all_menus.sql**
   - 初始化所有菜单（系统管理模块 + 研发工具模块）
   - 包含所有的目录、菜单、按钮权限
   - 为超级管理员分配所有权限

---

## ✅ 前端使用示例

### 1. 菜单显示

侧边栏自动根据用户权限显示菜单：
- 目录：显示箭头，可展开/收起
- 菜单：无箭头，点击跳转

### 2. 按钮权限控制

```typescript
import { PermissionButton } from '@/hooks/usePermission';

// 在列表页面中
<PermissionButton permission="system:user:create">
  <Button type="primary" onClick={handleAdd}>
    新增用户
  </Button>
</PermissionButton>

<PermissionButton permission="system:user:edit">
  <Button onClick={() => handleEdit(record)}>编辑</Button>
</PermissionButton>

<PermissionButton permission="system:user:delete">
  <Button danger onClick={() => handleDelete(record)}>删除</Button>
</PermissionButton>
```

### 3. 菜单数据获取

```typescript
// MenuContext.tsx
const { menus, permissions, loading } = useMenu();

// menus: 菜单树（只包含 type=1 和 type=2）
// permissions: 权限列表（包含所有 permission 字段）
```

---

## 🔍 常见问题

### Q1: 为什么菜单类型有箭头？

**A**: 检查以下几点：
1. 数据库中该菜单的 `type` 是否为 2（菜单类型）
2. 后端是否过滤了 `type=3` 的按钮
3. 前端 `convertMenus` 是否只对 `type=1` 递归处理子菜单

### Q2: 如何添加新的菜单？

**A**: 
1. 在数据库 `system_menu` 表中插入记录
2. 设置正确的 `type`、`parent_id`、`path`、`component`
3. 创建对应的前端页面文件（按照 `component` 字段）
4. 在 `system_role_menu` 表中为角色分配权限

### Q3: 按钮权限如何生效？

**A**:
1. 在 `system_menu` 表中创建 `type=3` 的按钮记录
2. 设置 `permission` 字段（如：`system:user:create`）
3. 为角色分配该按钮权限
4. 前端使用 `<PermissionButton permission="system:user:create">` 包裹按钮

---

## 📚 相关文档

- [动态菜单系统实现](DYNAMIC_MENU_SYSTEM.md)
- [树形结构工具使用](TREE_UTIL_USAGE.md)
- [Service 层设计规范](SERVICE_RULE_CHECKLIST.md)

---

**最后更新时间**：2025-10-03

