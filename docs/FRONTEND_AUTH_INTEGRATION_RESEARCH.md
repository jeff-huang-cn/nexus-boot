# 前端Spring Security认证集成研究报告

## 一、现状分析

### 1.1 技术栈
- **框架**: React 19 + TypeScript
- **UI库**: Ant Design 5
- **路由**: React Router DOM 7
- **HTTP客户端**: Axios 1.12
- **状态管理**: React Context (MenuContext)

### 1.2 现有架构分析

#### 目录结构
```
src/
├── components/
│   └── Layout/
│       └── AppLayout.tsx          # 布局组件（侧边栏、头部）
├── contexts/
│   └── MenuContext.tsx            # 菜单上下文（管理菜单和权限）
├── hooks/
│   └── usePermission.ts           # 权限检查Hook
├── pages/                         # 页面组件
├── router/
│   ├── AppRoutes.tsx              # 路由配置
│   └── DynamicRoutes.tsx          # 动态路由
├── services/                      # API服务
├── utils/
│   └── request.ts                 # axios封装
└── App.tsx                        # 应用入口
```

#### 现有功能
1. **request.ts**
   - 已配置axios实例
   - 有基础的请求/响应拦截器
   - **缺少**: Token管理、401跳转、Token刷新

2. **MenuContext.tsx**
   - 管理菜单树和权限列表
   - 从后端加载所有菜单（未过滤）
   - 权限存储在localStorage
   - **缺少**: 基于登录用户的菜单获取

3. **usePermission.ts**
   - 提供权限检查方法
   - 已实现: hasPermission, hasAnyPermission, hasAllPermissions
   - **可直接使用**

4. **AppRoutes.tsx**
   - 静态路由配置
   - **缺少**: 路由守卫、登录验证

## 二、Spring Security默认登录端点分析

### 2.1 登录端点
- **默认端点**: `POST /login`
- **请求参数**: 
  - username: 用户名
  - password: 密码
- **Content-Type**: `application/x-www-form-urlencoded` 或 `application/json`

### 2.2 响应格式
根据项目配置，通常返回JWT Token：
```json
{
  "code": 200,
  "data": {
    "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200
  },
  "message": "登录成功"
}
```

### 2.3 JWT Token结构
JWT包含三部分（Header.Payload.Signature），Payload示例：
```json
{
  "sub": "admin",                    // 用户名
  "userId": 1,                       // 用户ID
  "roles": ["ADMIN", "USER"],        // 角色列表
  "permissions": ["system:user:create", "system:user:delete"],  // 权限列表
  "exp": 1704067200,                 // 过期时间（Unix时间戳）
  "iat": 1704060000                  // 签发时间
}
```

### 2.4 后续请求验证
- **Header**: `Authorization: Bearer <token>`
- 后端Spring Security自动验证Token并初始化SecurityContext

## 三、前端改造方案

### 3.1 整体架构设计

```
┌─────────────────────────────────────────────────────────────┐
│                        应用入口 (App.tsx)                    │
│                                                               │
│  ┌───────────────────────────────────────────────────────┐  │
│  │              AuthProvider (认证上下文)                 │  │
│  │  - 管理登录状态                                        │  │
│  │  - 管理用户信息                                        │  │
│  │  - 管理Token                                           │  │
│  │  - 提供登录/登出方法                                   │  │
│  └───────────────────────────────────────────────────────┘  │
│                            ▼                                  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │              RouteGuard (路由守卫)                     │  │
│  │  - 检查是否登录                                        │  │
│  │  - 未登录重定向到登录页                                │  │
│  │  - Token过期检测                                       │  │
│  └───────────────────────────────────────────────────────┘  │
│                            ▼                                  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │            MenuProvider (菜单上下文)                   │  │
│  │  - 加载用户菜单                                        │  │
│  │  - 加载用户权限                                        │  │
│  └───────────────────────────────────────────────────────┘  │
│                            ▼                                  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                   应用布局 & 路由                      │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                      Axios拦截器                             │
│  请求拦截: 自动添加Authorization Header                      │
│  响应拦截: 401错误→清空Token→跳转登录                        │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 核心模块设计

#### 3.2.1 Token管理器 (utils/auth.ts)
```typescript
功能：
- parseJwtToken(): 解析JWT获取Payload
- getToken(): 从localStorage获取Token
- setToken(): 存储Token
- removeToken(): 清除Token
- isTokenExpired(): 检查Token是否过期
- getUserInfo(): 从Token获取用户信息
- getPermissions(): 从Token获取权限列表
- getRoles(): 从Token获取角色列表
```

#### 3.2.2 认证上下文 (contexts/AuthContext.tsx)
```typescript
功能：
- 状态管理: isAuthenticated, userInfo, loading
- login(username, password): 登录方法
- logout(): 登出方法
- checkAuth(): 检查登录状态
- refreshToken(): 刷新Token（可选）
```

#### 3.2.3 路由守卫 (components/RouteGuard.tsx)
```typescript
功能：
- 包装路由组件
- 检查是否已登录
- 未登录重定向到/login
- Token过期检测和处理
```

#### 3.2.4 登录页面 (pages/Login.tsx)
```typescript
功能：
- 用户名/密码输入表单
- 调用AuthContext.login()
- 登录成功跳转到首页
- 错误提示
```

#### 3.2.5 请求拦截器增强 (utils/request.ts)
```typescript
请求拦截器：
- 自动添加 Authorization: Bearer <token>
- 排除登录接口

响应拦截器：
- 401错误 → 清空Token → 跳转登录页
- 403错误 → 提示无权限
- Token过期自动刷新（可选）
```

### 3.3 权限控制方案

#### 3.3.1 按钮权限控制
使用现有的 `usePermission` Hook：
```tsx
const { hasPermission } = usePermission();

{hasPermission('system:user:create') && (
  <Button onClick={handleCreate}>新增</Button>
)}
```

#### 3.3.2 菜单权限过滤
修改 `MenuContext.tsx`：
- 登录成功后调用 `GET /api/user/menus` 获取用户菜单
- 只返回用户有权限的菜单
- 从JWT Token中提取权限列表

#### 3.3.3 路由权限控制
```typescript
// 方案1: 全局路由守卫（推荐）
所有路由都需要登录，除了 /login

// 方案2: 细粒度路由权限（可选）
特定路由需要特定权限
```

## 四、实施方案

### 4.1 需要新增的文件

```
src/
├── contexts/
│   └── AuthContext.tsx                 # [新增] 认证上下文
├── pages/
│   └── Login.tsx                       # [新增] 登录页面
├── components/
│   └── RouteGuard.tsx                  # [新增] 路由守卫
├── services/
│   └── auth.ts                         # [新增] 认证API
└── utils/
    └── auth.ts                         # [新增] Token管理工具
```

### 4.2 需要修改的文件

```
src/
├── App.tsx                             # [修改] 添加AuthProvider
├── router/
│   └── AppRoutes.tsx                   # [修改] 添加路由守卫
├── contexts/
│   └── MenuContext.tsx                 # [修改] 改为获取用户菜单
└── utils/
    └── request.ts                      # [修改] 添加Token拦截器
```

### 4.3 需要的npm包

```json
{
  "jwt-decode": "^4.0.0"  // JWT解析库（推荐使用）
}
```

或者使用原生方法解析JWT（无需额外依赖）：
```typescript
function parseJwt(token: string) {
  const base64Url = token.split('.')[1];
  const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
  const jsonPayload = decodeURIComponent(
    atob(base64)
      .split('')
      .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
      .join('')
  );
  return JSON.parse(jsonPayload);
}
```

## 五、后端配置要求

### 5.1 Spring Security配置

1. **登录端点**: `/login` (POST)
   - 接受JSON格式: `{"username": "", "password": ""}`
   - 返回JWT Token

2. **JWT配置**
   - 使用RS256算法
   - Token有效期: 2小时（建议）
   - 在Payload中包含:
     ```json
     {
       "sub": "username",
       "userId": 1,
       "roles": ["ADMIN"],
       "permissions": ["system:user:create", ...],
       "exp": 1704067200,
       "iat": 1704060000
     }
     ```

3. **跨域配置**
   - 允许前端域名访问
   - 允许 `Authorization` Header

4. **Token验证端点** (可选)
   - `GET /api/auth/validate` - 验证Token是否有效
   - `POST /api/auth/refresh` - 刷新Token

5. **用户菜单端点**
   - `GET /api/user/menus` - 获取当前用户的菜单树（已过滤权限）

### 5.2 响应格式统一

```json
{
  "code": 200,
  "data": { ... },
  "message": "成功"
}
```

错误格式：
```json
{
  "code": 401,
  "data": null,
  "message": "未登录或Token已过期"
}
```

## 六、安全性考虑

### 6.1 Token存储
- **推荐**: localStorage（简单，适合大多数场景）
- **更安全**: httpOnly Cookie（防止XSS，需要后端配合）
- **不推荐**: sessionStorage（刷新页面会丢失）

### 6.2 XSS防护
- React默认防XSS
- 使用 `dangerouslySetInnerHTML` 时要格外小心
- Token解析后的数据不要直接渲染

### 6.3 CSRF防护
- JWT Token本身不需要CSRF Token
- 确保Token只通过Authorization Header传输（不使用Cookie）

### 6.4 Token过期处理
- 方案1: Token过期后跳转登录页（简单）
- 方案2: 使用Refresh Token自动续期（复杂但用户体验好）

## 七、用户体验优化

### 7.1 记住登录状态
- Token存储在localStorage
- 刷新页面自动恢复登录状态
- 提供"记住我"选项（可选）

### 7.2 加载状态
- AuthContext提供loading状态
- 应用启动时检查Token有效性
- 显示加载动画避免闪烁

### 7.3 错误提示
- 登录失败: 显示具体错误信息
- Token过期: 提示并跳转登录页
- 权限不足: 提示无权限访问

### 7.4 自动登出
- Token过期自动登出
- 长时间无操作自动登出（可选）
- 多标签页同步登出状态（可选）

## 八、测试要点

### 8.1 认证流程测试
- [ ] 正常登录流程
- [ ] 错误的用户名/密码
- [ ] Token过期后的行为
- [ ] 登出后无法访问受保护资源

### 8.2 权限测试
- [ ] 按钮权限显示/隐藏
- [ ] 菜单权限过滤
- [ ] 无权限的路由访问
- [ ] 不同角色的权限差异

### 8.3 边界情况测试
- [ ] Token被手动删除
- [ ] Token格式错误
- [ ] 网络请求失败
- [ ] 刷新页面保持登录状态
- [ ] 多标签页同步（可选）

## 九、参考资料

- Spring Security JWT配置: https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html
- JWT官方文档: https://jwt.io/
- React Router 7认证示例: https://reactrouter.com/en/main/start/examples
- Ant Design Form表单: https://ant.design/components/form-cn

