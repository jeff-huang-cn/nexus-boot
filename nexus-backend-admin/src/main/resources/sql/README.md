# SQL 脚本执行说明

## 📋 脚本执行顺序（全新初始化）

```sql
-- 第1步：代码生成基础表
init_codegen.sql

-- 第2步：权限管理基础表和超级管理员角色
V20251003_2__init_permission.sql

-- 第3步：字典表
V20251003_3__init_dict.sql

-- 第4步：完整菜单数据（系统管理+研发工具）
V20251003_4__init_all_menus.sql
```

## 🎯 路径约定（重要）

### **前端组件路径约定**

数据库 `component` 字段 = 相对于 `pages/` 目录的路径（不含 `.tsx`）

| 菜单名称 | 数据库 path | 数据库 component | 前端文件路径 |
|---------|------------|-----------------|-------------|
| 用户管理 | `/system/user` | `system/user/index` | `pages/system/user/index.tsx` |
| 角色管理 | `/system/role` | `system/role/index` | `pages/system/role/index.tsx` |
| 菜单管理 | `/system/menu` | `system/menu/index` | `pages/system/menu/index.tsx` |
| 代码生成 | `/dev/codegen` | `dev/codegen/index` | `pages/dev/codegen/index.tsx` |

### **核心规则**
1. **目录（type=1）**: `component='Layout'`，不需要实际文件
2. **菜单（type=2）**: `component` 对应实际的 React 组件文件
3. **按钮（type=3）**: 无 `component`，仅用于权限控制

## 🗂️ 前端目录结构

```
ui/src/pages/
├── system/              # 系统管理模块
│   ├── user/
│   │   └── index.tsx    # 用户管理页面
│   ├── role/
│   │   ├── index.tsx    # 角色管理页面
│   │   └── AssignMenuModal.tsx
│   └── menu/
│       └── index.tsx    # 菜单管理页面
└── dev/                 # 研发工具模块
    └── codegen/
        ├── index.tsx         # 代码生成列表
        ├── ImportTable.tsx   # 导入表
        ├── EditTable.tsx     # 编辑表
        └── PreviewCode.tsx   # 预览代码
```

## 📊 数据库表关系

```
system_role (角色)
    ↓ (1:N)
system_role_menu (角色菜单关联)
    ↓ (N:1)
system_menu (菜单)

system_user (用户)
    ↓ (1:N)
system_user_role (用户角色关联)
    ↓ (N:1)
system_role (角色)
```

## 🎯 初始化后的完整菜单结构

```
系统管理 (/system) - 目录 [Layout]
├── 用户管理 (/system/user) - 菜单 [system/user/index]
│   ├── 查询 (system:user:query) - 按钮
│   ├── 新增 (system:user:create) - 按钮
│   ├── 编辑 (system:user:update) - 按钮
│   └── 删除 (system:user:delete) - 按钮
├── 角色管理 (/system/role) - 菜单 [system/role/index]
│   ├── 查询 (system:role:query) - 按钮
│   ├── 新增 (system:role:create) - 按钮
│   ├── 编辑 (system:role:update) - 按钮
│   ├── 删除 (system:role:delete) - 按钮
│   └── 分配权限 (system:role:assign-menu) - 按钮
└── 菜单管理 (/system/menu) - 菜单 [system/menu/index]
    ├── 查询 (system:menu:query) - 按钮
    ├── 新增 (system:menu:create) - 按钮
    ├── 编辑 (system:menu:update) - 按钮
    └── 删除 (system:menu:delete) - 按钮

研发工具 (/dev) - 目录 [Layout]
└── 代码生成 (/dev/codegen) - 菜单 [dev/codegen/index]
    ├── 查询 (codegen:table:query) - 按钮
    ├── 导入 (codegen:table:import) - 按钮
    ├── 编辑 (codegen:table:update) - 按钮
    ├── 删除 (codegen:table:delete) - 按钮
    ├── 预览 (codegen:table:preview) - 按钮
    └── 生成 (codegen:table:generate) - 按钮
```

## 添加新菜单的步骤

### 1. 在数据库插入菜单记录

```sql
-- 示例：添加字典管理菜单
INSERT INTO `system_menu` 
(`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `creator`) 
VALUES ('字典管理', 'system:dict:query', 2, 4, @system_dir_id, '/system/dict', 'book', 'system/dict/index', 'SystemDict', 'system');
```

### 2. 在前端创建对应的组件文件

```
创建文件：pages/system/dict/index.tsx
```

### 3. 系统自动识别和加载 ✨

无需修改路由配置，前端会根据菜单数据自动生成路由！

## 🔧 清理脚本（开发环境）

如需重新初始化，先执行清理脚本：

```sql
-- 清理权限相关数据
DELETE FROM `system_user_role`;
DELETE FROM `system_role_menu`;
DELETE FROM `system_menu`;
DELETE FROM `system_role`;

-- 重置自增ID
ALTER TABLE `system_menu` AUTO_INCREMENT = 1;
ALTER TABLE `system_role` AUTO_INCREMENT = 1;
ALTER TABLE `system_role_menu` AUTO_INCREMENT = 1;
ALTER TABLE `system_user_role` AUTO_INCREMENT = 1;
```

## ⚠️ 注意事项

1. **执行顺序**: 必须按照编号顺序执行
2. **重复执行**: V20251003_4 可以重复执行，不会产生重复数据
3. **路径一致性**: 确保数据库 `component` 字段与前端文件路径一致
4. **生产环境**: 执行前务必备份数据库
5. **ID自增**: 所有ID由数据库自动分配，不要手动指定

## 🚀 后续扩展

添加新业务模块时：

1. 设计菜单结构（目录-菜单-按钮）
2. 编写SQL脚本（参考V20251003_4格式）
3. 按约定创建前端页面文件
4. 执行SQL脚本

**无需修改任何路由或映射配置！** 🎉

