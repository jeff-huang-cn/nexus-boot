# 权限系统设计说明

## 📋 核心设计原则

### 1. 菜单类型与权限的关系

| 类型 | type值 | permission字段 | 说明 |
|-----|--------|---------------|------|
| **目录** | 1 | ❌ 不设置 | 用于菜单分组，无需权限标识 |
| **菜单** | 2 | ❌ 不设置 | 导航页面，无需权限标识 |
| **按钮** | 3 | ✅ 必须设置 | 操作权限，需要权限标识 |

### 2. 权限控制方式

```
用户 → 角色 → 菜单（包含按钮）
```

**权限判断流程**：
1. 用户登录后，获取用户的所有角色
2. 通过角色获取所有有权限的菜单ID（包括目录、菜单、按钮）
3. 根据菜单ID过滤，只显示用户有权限的菜单结构
4. 根据按钮的permission字段，控制页面内的操作按钮显示

---

## 🎯 设计理念

### 为什么目录和菜单不需要 permission？

1. **目录和菜单是导航结构**
   - 目录：菜单分组（如：系统管理、研发工具）
   - 菜单：页面入口（如：用户管理、代码生成）
   - 这些只是组织结构，不涉及具体操作

2. **权限通过角色-菜单关联控制**
   - 在 `system_role_menu` 表中关联角色和菜单
   - 如果角色有某个菜单的ID，则该角色的用户可以看到该菜单
   - 无需在菜单表中设置 permission 字段

3. **按钮才是真正的权限点**
   - 按钮代表具体操作（查询、新增、编辑、删除等）
   - 每个按钮都有独立的 permission 标识
   - 前端使用 `usePermission` Hook 控制按钮显示

---

## 🔧 数据库设计

### system_menu 表字段说明

```sql
CREATE TABLE `system_menu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL COMMENT '菜单名称',
  
  -- ⚠️ permission 字段：只有 type=3（按钮）才设置
  `permission` VARCHAR(100) DEFAULT NULL COMMENT '权限标识（仅按钮使用）',
  
  -- type 决定是否需要 permission
  `type` TINYINT NOT NULL COMMENT '菜单类型：1-目录 2-菜单 3-按钮',
  
  `sort` INT DEFAULT 0,
  `parent_id` BIGINT DEFAULT 0,
  `path` VARCHAR(200) DEFAULT NULL COMMENT '路由地址（目录和菜单使用）',
  `icon` VARCHAR(100) DEFAULT NULL,
  `component` VARCHAR(255) DEFAULT NULL COMMENT '组件路径（菜单使用）',
  ...
);
```

### 示例数据

```sql
-- 目录：没有 permission
INSERT INTO `system_menu` 
(`name`, `type`, `path`, `icon`) 
VALUES ('系统管理', 1, '/system', 'setting');

-- 菜单：没有 permission
INSERT INTO `system_menu` 
(`name`, `type`, `parent_id`, `path`, `icon`, `component`) 
VALUES ('用户管理', 2, 1, '/system/user', 'user', 'system/user/index');

-- 按钮：有 permission
INSERT INTO `system_menu` 
(`name`, `type`, `parent_id`, `permission`) 
VALUES ('新增', 3, 2, 'system:user:create');

INSERT INTO `system_menu` 
(`name`, `type`, `parent_id`, `permission`) 
VALUES ('编辑', 3, 2, 'system:user:update');
```

---

## 🌲 菜单结构示例

### 数据库结构

```
id=1  系统管理    type=1  permission=null       path=/system
  ├─ id=2  用户管理  type=2  permission=null       path=/system/user
  │   ├─ id=3  查询    type=3  permission=system:user:query
  │   ├─ id=4  新增    type=3  permission=system:user:create
  │   ├─ id=5  编辑    type=3  permission=system:user:update
  │   └─ id=6  删除    type=3  permission=system:user:delete
  │
  ├─ id=7  角色管理  type=2  permission=null       path=/system/role
  │   ├─ id=8  查询    type=3  permission=system:role:query
  │   ├─ id=9  新增    type=3  permission=system:role:create
  │   └─ ...
  │
  └─ id=13 菜单管理  type=2  permission=null       path=/system/menu
      └─ ...
```

### 角色权限分配

**超级管理员**：拥有所有菜单ID（1-13），可以看到所有菜单和按钮

```sql
-- system_role_menu 表
role_id=1, menu_id=1  (系统管理目录)
role_id=1, menu_id=2  (用户管理菜单)
role_id=1, menu_id=3  (查询按钮)
role_id=1, menu_id=4  (新增按钮)
...
```

**普通用户**：只拥有部分菜单ID，只能看到分配的菜单和按钮

```sql
-- system_role_menu 表
role_id=2, menu_id=1  (系统管理目录 - 显示)
role_id=2, menu_id=2  (用户管理菜单 - 显示)
role_id=2, menu_id=3  (查询按钮 - 可查询)
-- 没有 menu_id=4，所以"新增"按钮不显示
```

---

## 💻 前端实现

### 1. 菜单树渲染（后端过滤）

```java
// MenuController.java
@GetMapping("/tree")
public Result<List<MenuRespVO>> getMenuTree() {
    // 获取当前用户有权限的所有菜单
    List<MenuDO> menuList = menuService.getMenuList();
    
    // 过滤掉按钮（type=3），只返回目录和菜单
    List<MenuRespVO> menuVOList = menuList.stream()
        .filter(menu -> menu.getType() != 3)
        .map(menu -> BeanUtil.copyProperties(menu, MenuRespVO.class))
        .collect(Collectors.toList());
    
    // 构建树形结构
    return Result.success(TreeUtils.buildTree(...));
}
```

### 2. 按钮权限控制（前端过滤）

```typescript
// 前端获取所有权限标识
const { permissions } = useMenu();
// permissions: ['system:user:query', 'system:user:create', ...]

// 使用权限控制按钮显示
<PermissionButton permission="system:user:create">
  <Button type="primary">新增用户</Button>
</PermissionButton>

// usePermission Hook 实现
export const usePermission = () => {
  const { permissions } = useMenu();
  
  const hasPermission = (permission: string) => {
    return permissions.includes(permission);
  };
  
  return { hasPermission };
};

export const PermissionButton = ({ permission, children }) => {
  const { hasPermission } = usePermission();
  
  if (!hasPermission(permission)) {
    return null;
  }
  
  return children;
};
```

---

## 🔍 权限判断逻辑

### 后端 Service 层

```java
@Service
public class MenuServiceImpl implements MenuService {
    
    @Override
    public List<MenuDO> getMenuList() {
        // 1. 获取当前登录用户ID
        Long userId = SecurityUtils.getCurrentUserId();
        
        // 2. 获取用户的所有角色ID
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        
        // 3. 获取角色有权限的所有菜单ID
        List<Long> menuIds = roleMenuMapper.selectMenuIdsByRoleIds(roleIds);
        
        // 4. 查询这些菜单的详细信息
        if (menuIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        return menuMapper.selectBatchIds(menuIds);
    }
}
```

### 前端权限上下文

```typescript
// MenuContext.tsx
export const MenuProvider = ({ children }) => {
  const [menus, setMenus] = useState<Menu[]>([]);
  const [permissions, setPermissions] = useState<string[]>([]);
  
  useEffect(() => {
    // 加载用户菜单树
    menuApi.getMenuTree().then(data => {
      setMenus(data);
      
      // 提取所有 permission（递归遍历）
      const allPermissions = extractPermissions(data);
      setPermissions(allPermissions);
    });
  }, []);
  
  return (
    <MenuContext.Provider value={{ menus, permissions }}>
      {children}
    </MenuContext.Provider>
  );
};
```

---

## 📊 对比：新旧设计

### ❌ 旧设计（不推荐）

```sql
-- 菜单也有 permission 字段
INSERT INTO `system_menu` 
VALUES ('用户管理', 'system:user:query', 2, ...);
```

**问题**：
1. 菜单的 permission 和查询按钮的 permission 重复
2. 权限判断逻辑混乱（是判断菜单的permission还是按钮的？）
3. 后端需要额外的逻辑来区分菜单权限和按钮权限

### ✅ 新设计（推荐）

```sql
-- 菜单没有 permission
INSERT INTO `system_menu` 
VALUES ('用户管理', NULL, 2, ...);

-- 只有按钮有 permission
INSERT INTO `system_menu` 
VALUES ('查询', 'system:user:query', 3, ...);
```

**优势**：
1. 职责清晰：菜单只负责导航，按钮负责权限
2. 通过角色-菜单关联控制菜单显示
3. 通过按钮的permission控制操作权限
4. 逻辑简单，易于理解和维护

---

## 🚀 最佳实践

### 1. 菜单命名规范

- 目录：简短明了（系统管理、研发工具）
- 菜单：功能名称 + "管理"（用户管理、角色管理）
- 按钮：操作动作（查询、新增、编辑、删除）

### 2. Permission 命名规范

格式：`模块:功能:操作`

**示例**：
```
system:user:query        # 系统管理-用户管理-查询
system:user:create       # 系统管理-用户管理-新增
system:user:update       # 系统管理-用户管理-编辑
system:user:delete       # 系统管理-用户管理-删除
system:role:assign-menu  # 系统管理-角色管理-分配权限
codegen:table:generate   # 代码生成-表管理-生成代码
```

### 3. 角色分配原则

**最小权限原则**：
- 只分配必要的菜单和按钮
- 新用户默认只有查询权限
- 管理员拥有完整的增删改查权限

**示例**：

```sql
-- 只读用户角色
INSERT INTO `system_role_menu` (role_id, menu_id)
SELECT 2, id FROM `system_menu` 
WHERE `type` IN (1, 2)  -- 只有目录和菜单
   OR `permission` LIKE '%:query';  -- 只有查询按钮

-- 管理员角色
INSERT INTO `system_role_menu` (role_id, menu_id)
SELECT 1, id FROM `system_menu`;  -- 所有菜单和按钮
```

---

## 📚 相关文档

- [菜单结构设计](MENU_STRUCTURE.md)
- [树形结构工具](TREE_UTIL_USAGE.md)
- [Service层设计规范](SERVICE_RULE_CHECKLIST.md)

---

**最后更新时间**：2025-10-03

