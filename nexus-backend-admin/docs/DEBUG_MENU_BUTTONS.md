# 菜单按钮权限显示问题排查指南

## 🔍 问题描述

菜单管理页面没有显示菜单下的按钮权限（查询、新增、编辑、删除等）

---

## 📋 排查步骤

### 步骤1：检查数据库是否有按钮数据

```sql
-- 查看所有菜单数据，包括按钮
SELECT 
    id,
    name,
    CASE type 
        WHEN 1 THEN '目录'
        WHEN 2 THEN '菜单'
        WHEN 3 THEN '按钮'
    END as type_name,
    type,
    parent_id,
    permission,
    path
FROM system_menu
ORDER BY parent_id, sort;
```

**预期结果**：应该看到很多 `type=3` 的按钮记录，如：
```
id  | name   | type_name | type | parent_id | permission           | path
----|--------|-----------|------|-----------|----------------------|------
1   | 系统管理 | 目录      | 1    | 0         | NULL                 | /system
2   | 用户管理 | 菜单      | 2    | 1         | NULL                 | /system/user
3   | 查询    | 按钮      | 3    | 2         | system:user:query    | NULL
4   | 新增    | 按钮      | 3    | 2         | system:user:create   | NULL
5   | 编辑    | 按钮      | 3    | 2         | system:user:update   | NULL
6   | 删除    | 按钮      | 3    | 2         | system:user:delete   | NULL
```

**如果没有按钮数据**，执行初始化脚本：
```sql
-- 执行 V20251003_4__init_all_menus.sql
```

---

### 步骤2：检查后端接口返回数据

#### 测试方法1：浏览器直接访问

打开浏览器访问：
```
http://localhost:8080/api/system/menu/tree/full
```

**预期返回**（JSON格式）：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "系统管理",
      "type": 1,
      "children": [
        {
          "id": 2,
          "name": "用户管理",
          "type": 2,
          "path": "/system/user",
          "children": [
            {
              "id": 3,
              "name": "查询",
              "type": 3,
              "permission": "system:user:query"
            },
            {
              "id": 4,
              "name": "新增",
              "type": 3,
              "permission": "system:user:create"
            }
          ]
        }
      ]
    }
  ]
}
```

**检查要点**：
- ✅ 菜单（type=2）下应该有 `children` 数组
- ✅ `children` 中应该包含 `type=3` 的按钮
- ✅ 按钮应该有 `permission` 字段

#### 测试方法2：使用 curl 或 Postman

```bash
curl http://localhost:8080/api/system/menu/tree/full
```

---

### 步骤3：检查前端网络请求

1. 打开浏览器开发者工具（F12）
2. 切换到 Network 标签页
3. 刷新菜单管理页面
4. 找到 `tree/full` 请求
5. 查看 Response 数据

**检查要点**：
- 请求路径是否正确：`/api/system/menu/tree/full`
- 响应状态码是否为 200
- 响应数据中是否包含 `type=3` 的按钮

---

### 步骤4：检查前端代码

#### 检查 API 调用

**文件**：`ui/src/pages/system/menu/index.tsx`

```typescript
// 第151行，应该调用 getFullMenuTree
const result = await menuApi.getFullMenuTree() as Menu[];
```

#### 检查 API 定义

**文件**：`ui/src/services/menu/index.ts`

```typescript
/**
 * 获取完整菜单树（用于菜单管理页面，包含按钮）
 */
getFullMenuTree: () => {
  return request.get<Menu[]>(`${API_BASE}/tree/full`);
},
```

**检查要点**：
- API_BASE 应该是 `/system/menu`
- 完整路径应该是 `/system/menu/tree/full`
- request 的 baseURL 已经包含 `/api`

---

### 步骤5：清除缓存

#### 清除浏览器缓存
1. 打开开发者工具（F12）
2. 右键点击刷新按钮
3. 选择"清空缓存并硬性重新加载"

#### 清除前端构建缓存
```bash
cd nexus-boot/nexus-backend-admin/ui
rm -rf node_modules/.cache
rm -rf build
npm run build
```

---

## 🔧 快速修复方案

### 方案1：重新初始化数据库

```sql
-- 1. 清空菜单表
TRUNCATE TABLE system_role_menu;
TRUNCATE TABLE system_menu;

-- 2. 重新执行初始化脚本
-- 执行 V20251003_2__init_permission.sql
-- 执行 V20251003_4__init_all_menus.sql

-- 3. 验证按钮数据
SELECT COUNT(*) as button_count 
FROM system_menu 
WHERE type = 3;
-- 应该返回 > 0 的数量
```

### 方案2：手动添加测试数据

```sql
-- 获取用户管理菜单ID
SET @user_menu_id = (SELECT id FROM system_menu WHERE path = '/system/user' LIMIT 1);

-- 手动添加按钮权限
INSERT INTO system_menu (name, permission, type, sort, parent_id, creator)
VALUES 
  ('查询', 'system:user:query', 3, 1, @user_menu_id, 'system'),
  ('新增', 'system:user:create', 3, 2, @user_menu_id, 'system'),
  ('编辑', 'system:user:update', 3, 3, @user_menu_id, 'system'),
  ('删除', 'system:user:delete', 3, 4, @user_menu_id, 'system');

-- 重启后端服务，刷新前端
```

---

## 🐛 常见问题

### Q1: 后端接口404

**检查**：
```
访问 http://localhost:8080/api/system/menu/tree/full
```

**如果404**：
- 检查 MenuController 的 @RequestMapping 是否为 `/system/menu`
- 检查是否有 @GetMapping("/tree/full") 注解
- 重启后端服务

### Q2: 返回数据为空

**检查**：
```sql
SELECT * FROM system_menu WHERE type IN (1, 2, 3);
```

**如果为空**：
- 执行初始化脚本
- 检查 SQL 脚本是否有错误

### Q3: 前端显示空白

**检查浏览器控制台**：
- 是否有JavaScript错误
- 是否有网络请求错误
- 检查 Network 标签中的请求响应

---

## ✅ 验证清单

运行以下检查，找到问题所在：

- [ ] 数据库中有 `type=3` 的按钮数据
- [ ] 访问 `/api/system/menu/tree/full` 返回包含按钮的数据
- [ ] 前端网络请求成功（状态码200）
- [ ] 前端正确调用了 `menuApi.getFullMenuTree()`
- [ ] 浏览器控制台没有错误
- [ ] 清除了浏览器缓存

---

## 🚀 快速测试命令

```bash
# 1. 检查后端接口
curl http://localhost:8080/api/system/menu/tree/full | json_pp

# 2. 检查数据库
mysql -u root -p -e "SELECT id, name, type, parent_id, permission FROM system_menu WHERE type = 3 LIMIT 10;"

# 3. 重启服务
# 停止后端，重新运行
# 刷新前端（Ctrl+F5 强制刷新）
```

---

**请按照上述步骤逐一排查，找到问题所在！**

