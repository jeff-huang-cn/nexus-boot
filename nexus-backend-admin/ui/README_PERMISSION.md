# 权限管理前端使用指南

## 📦 已实现功能

### 1. 菜单管理
- ✅ 树形表格展示菜单
- ✅ 新增/编辑/删除菜单
- ✅ 支持多级菜单嵌套
- ✅ 菜单类型：目录、菜单、按钮

### 2. 角色管理
- ✅ 角色列表展示
- ✅ 新增/编辑/删除角色
- ✅ 分配菜单权限（树形复选框）

---

## 🚀 快速开始

### 1. 访问页面

确保后端服务已启动，然后访问：

- 菜单管理：`http://localhost:3000/system/menu`
- 角色管理：`http://localhost:3000/system/role`

### 2. 菜单管理操作流程

#### 创建根菜单（目录）
1. 点击"新增菜单"按钮
2. 填写菜单信息：
   - 菜单名称：系统管理
   - 菜单类型：目录
   - 路由地址：/system
   - 菜单图标：setting
   - 显示顺序：1
3. 点击"确定"保存

#### 创建子菜单
1. 在目录行点击"新增"按钮
2. 填写菜单信息：
   - 菜单名称：用户管理
   - 菜单类型：菜单
   - 权限标识：system:user:query
   - 路由地址：user
   - 组件路径：system/user/index
   - 组件名称：SystemUser
3. 点击"确定"保存

#### 创建按钮权限
1. 在菜单行点击"新增"按钮
2. 填写按钮信息：
   - 菜单名称：新增
   - 菜单类型：按钮
   - 权限标识：system:user:create
3. 点击"确定"保存

### 3. 角色管理操作流程

#### 创建角色
1. 点击"新增角色"按钮
2. 填写角色信息：
   - 角色名称：管理员
   - 角色编码：admin
   - 显示顺序：1
   - 角色状态：启用
   - 备注：系统管理员
3. 点击"确定"保存

#### 分配菜单权限
1. 在角色行点击"分配权限"按钮
2. 在树形复选框中选择要分配的菜单
3. 点击"确定"保存

---

## 📋 API 接口说明

### 菜单管理 API

```typescript
import { menuApi } from '@/services/menu';

// 获取菜单树
const menuTree = await menuApi.getMenuTree();

// 创建菜单
await menuApi.create({
  name: '用户管理',
  type: 2,
  parentId: 1,
  permission: 'system:user:query',
  path: 'user',
  component: 'system/user/index',
});

// 更新菜单
await menuApi.update(menuId, formData);

// 删除菜单
await menuApi.delete(menuId);

// 获取角色的菜单ID列表
const menuIds = await menuApi.getMenuIdsByRoleId(roleId);
```

### 角色管理 API

```typescript
import { roleApi } from '@/services/role';

// 获取角色列表
const roleList = await roleApi.getList();

// 创建角色
await roleApi.create({
  name: '管理员',
  code: 'admin',
  status: 1,
});

// 更新角色
await roleApi.update(roleId, formData);

// 删除角色
await roleApi.delete(roleId);

// 分配菜单
await roleApi.assignMenu({
  roleId: 1,
  menuIds: [1, 100, 1001, 1002],
});
```

---

## 🎨 组件说明

### MenuPage (`pages/system/menu/index.tsx`)

菜单管理主页面，使用 Ant Design 的 Tree Table 组件展示菜单树。

**主要功能：**
- 树形表格展示
- 默认展开所有节点
- 支持在任意节点下新增子菜单
- 编辑菜单信息
- 删除菜单（带二次确认）

**表单字段：**
- 菜单名称（必填）
- 菜单类型（必填：目录/菜单/按钮）
- 权限标识（按钮类型必填）
- 路由地址（菜单类型需要）
- 菜单图标
- 组件路径（菜单类型需要）
- 组件名称
- 显示顺序
- 菜单状态
- 是否可见
- 是否缓存
- 总是显示

### RolePage (`pages/system/role/index.tsx`)

角色管理主页面，使用 Ant Design 的 Table 组件展示角色列表。

**主要功能：**
- 表格展示角色列表
- 分页功能
- 新增/编辑角色
- 删除角色（系统内置角色不可删除）
- 分配菜单权限

**表单字段：**
- 角色名称（必填）
- 角色编码（必填）
- 显示顺序
- 角色状态
- 备注

### AssignMenuModal (`pages/system/role/AssignMenuModal.tsx`)

角色分配菜单权限弹窗组件。

**主要功能：**
- 树形复选框展示所有菜单
- 自动加载角色已分配的菜单并选中
- 默认展开所有节点
- 支持选中/取消选中菜单
- 保存分配结果

---

## 📝 类型定义

### Menu 类型

```typescript
export interface Menu {
  id?: number;
  name?: string;
  permission?: string;
  type?: number; // 1-目录 2-菜单 3-按钮
  sort?: number;
  parentId?: number;
  path?: string;
  icon?: string;
  component?: string;
  componentName?: string;
  status?: number; // 0-禁用 1-启用
  visible?: number;
  keepAlive?: number;
  alwaysShow?: number;
  dateCreated?: string;
  children?: Menu[]; // 子菜单
}
```

### Role 类型

```typescript
export interface Role {
  id?: number;
  name?: string;
  code?: string;
  sort?: number;
  status?: number; // 0-禁用 1-启用
  type?: number; // 1-系统内置 2-自定义
  remark?: string;
  dateCreated?: string;
}
```

---

## ⚠️ 注意事项

1. **菜单类型说明：**
   - 目录（type=1）：最顶级，无具体功能，用于组织菜单结构
   - 菜单（type=2）：可点击跳转的页面
   - 按钮（type=3）：页面内的操作权限

2. **权限标识命名规范：**
   - 格式：`模块:功能:操作`
   - 示例：`system:user:create`、`system:role:update`

3. **路由地址说明：**
   - 目录路由以 `/` 开头，如：`/system`
   - 菜单路由相对于父级，如：`user`（完整路径：`/system/user`）

4. **系统内置角色保护：**
   - type=1 的角色为系统内置，不可删除

5. **菜单删除限制：**
   - 有子菜单的节点不可删除，需先删除子菜单

---

## 🔧 后续开发建议

### 1. 动态路由加载
根据用户权限动态加载菜单路由：

```typescript
// 根据菜单数据生成路由
function generateRoutes(menus: Menu[]): RouteObject[] {
  return menus
    .filter(menu => menu.type !== 3) // 排除按钮
    .map(menu => ({
      path: menu.path,
      element: lazy(() => import(`@/pages/${menu.component}`)),
      children: menu.children ? generateRoutes(menu.children) : undefined,
    }));
}
```

### 2. 按钮权限控制
使用自定义 Hook 检查按钮权限：

```typescript
// usePermission.ts
export const usePermission = () => {
  const permissions = useSelector(state => state.user.permissions);
  
  const hasPermission = (permission: string) => {
    return permissions.includes(permission);
  };
  
  return { hasPermission };
};

// 使用
const { hasPermission } = usePermission();

{hasPermission('system:user:create') && (
  <Button onClick={handleCreate}>新增</Button>
)}
```

### 3. 菜单图标组件
创建图标映射组件：

```typescript
// IconRender.tsx
import * as Icons from '@ant-design/icons';

export const IconRender: React.FC<{ icon?: string }> = ({ icon }) => {
  if (!icon) return null;
  const Icon = Icons[icon as keyof typeof Icons];
  return Icon ? <Icon /> : null;
};
```

---

**📅 创建时间：** 2025-10-03  
**👨‍💻 作者：** nexus-codegen-team

