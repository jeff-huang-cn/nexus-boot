# 认证API使用指南

## 📋 API概述

AuthController提供了完整的认证功能，包括：
- ✅ 用户登录（返回JWT Token）
- ✅ 退出登录
- ✅ 获取当前登录用户信息
- 🚧 刷新Token（待实现）

---

## 🔐 认证架构

### 认证流程
```
1. 前端发送用户名/密码 → /admin/auth/login
2. Spring Security AuthenticationManager 验证凭证
3. 查询用户信息和按钮权限
4. 生成JWT Token（包含用户名和权限）
5. 返回Token和用户信息
6. 前端存储Token，后续请求携带Token
7. 后端JwtAuthenticationFilter验证Token
```

### 权限架构
```
UserDO (用户)
  ↓ (用户-角色关联)
RoleDO (角色)
  ↓ (角色-菜单关联)
MenuDO (菜单/按钮)
  ↓ (过滤按钮类型)
按钮权限 (如: system:user:create)
  ↓
JWT Token (authorities字段)
```

---

## 📡 API端点

### 1. 登录接口

**端点**: `POST /admin/auth/login`

**请求体**:
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "userId": 1,
    "username": "admin",
    "nickname": "管理员",
    "permissions": [
      "system:user:create",
      "system:user:update",
      "system:menu:list"
    ],
    "authorities": [
      "system:user:create",
      "system:user:update"
    ]
  }
}
```

**失败响应** (401):
```json
{
  "code": 401,
  "message": "用户名或密码错误"
}
```

---

### 2. 退出登录接口

**端点**: `POST /admin/auth/logout`

**请求头**:
```
Authorization: Bearer eyJhbGciOiJSUzI1NiJ9...
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "退出登录成功"
}
```

**说明**:
- JWT是无状态的，退出登录主要由前端清除Token实现
- 后端可以实现Token黑名单机制（待实现）

---

### 3. 获取当前用户信息

**端点**: `GET /admin/auth/userinfo`

**请求头**:
```
Authorization: Bearer eyJhbGciOiJSUzI1NiJ9...
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "userId": 1,
    "username": "admin",
    "nickname": "管理员",
    "email": "admin@example.com",
    "mobile": "13800138000",
    "sex": 1,
    "avatar": "https://...",
    "status": 1,
    "permissions": [
      "system:user:create",
      "system:user:update"
    ]
  }
}
```

---

### 4. 刷新Token接口（待实现）

**端点**: `POST /admin/auth/refresh`

**请求体**:
```json
{
  "refreshToken": "old_token_here"
}
```

**响应** (501):
```json
{
  "code": 501,
  "message": "Token刷新功能暂未实现"
}
```

---

## 🧪 测试方法

### 使用curl测试

#### 1. 登录
```bash
curl -X POST http://localhost:8080/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

#### 2. 获取用户信息
```bash
# 先保存Token
TOKEN="eyJhbGciOiJSUzI1NiJ9..."

curl -X GET http://localhost:8080/admin/auth/userinfo \
  -H "Authorization: Bearer $TOKEN"
```

#### 3. 退出登录
```bash
curl -X POST http://localhost:8080/admin/auth/logout \
  -H "Authorization: Bearer $TOKEN"
```

---

### 使用Postman测试

1. **创建Collection**: Nexus Auth API

2. **添加Environment变量**:
   - `baseUrl`: http://localhost:8080
   - `token`: (登录后自动设置)

3. **登录请求**:
   - Method: POST
   - URL: `{{baseUrl}}/admin/auth/login`
   - Body (JSON):
     ```json
     {
       "username": "admin",
       "password": "admin123"
     }
     ```
   - Tests (自动保存Token):
     ```javascript
     if (pm.response.code === 200) {
       const data = pm.response.json().data;
       pm.environment.set("token", data.accessToken);
     }
     ```

4. **其他请求**:
   - Authorization: Bearer Token
   - Token: `{{token}}`

---

## 🔧 核心实现说明

### 1. 认证流程 (login方法)

```java
// 1. Spring Security认证
Authentication auth = authenticationManager.authenticate(
    new UsernamePasswordAuthenticationToken(username, password)
);

// 2. 查询用户信息
UserDO user = userService.getUserByUsername(username);

// 3. 查询用户权限
Set<String> permissions = permissionService.getUserAllPermissions(user.getId());

// 4. 生成JWT Token（包含用户名和权限）
String token = jwtTokenGenerator.generateToken(auth);
```

### 2. 权限加载 (DatabaseUserDetailsServiceImpl)

```java
// 查询用户
UserDO user = userService.getUserByUsername(username);

// 查询用户的所有按钮权限
Set<String> permissions = permissionService.getUserAllPermissions(user.getId());

// 构建UserDetails（权限会放入authorities）
return User.builder()
    .username(user.getUsername())
    .password(user.getPassword())
    .authorities(permissions) // 按钮权限作为authorities
    .build();
```

### 3. JWT生成 (JwtTokenGenerator)

```java
// JWT Claims包含：
JwtClaimsSet claims = JwtClaimsSet.builder()
    .issuer("my-app")
    .issuedAt(now)
    .expiresAt(now.plus(2, ChronoUnit.HOURS)) // 2小时过期
    .subject(username) // 用户名
    .claim("authorities", authorities) // 权限（空格分隔）
    .build();
```

---

## 📌 注意事项

### 1. 密码加密
- 使用BCryptPasswordEncoder
- 密码存储格式：`{bcrypt}$2a$10$...`

### 2. Token有效期
- 默认：2小时
- 可在`JwtTokenGenerator`中调整

### 3. 权限格式
- 格式：`模块:功能:操作`
- 示例：`system:user:create`

### 4. CORS配置
- 已在`SecurityConfig.corsConfigurationSource()`中配置
- 允许所有origin（生产环境需要限制）

### 5. 安全配置
- `/admin/auth/**` 端点已在`SecurityConfig`中放行
- 无需Token即可访问登录接口

---

## 🚀 下一步开发

### Token黑名单机制
```java
// 退出登录时：
1. 从请求头获取Token
2. 解析Token，获取过期时间
3. 将Token存入Redis，设置TTL为剩余有效期
4. JwtAuthenticationFilter验证时检查黑名单
```

### Token刷新机制
```java
// 刷新Token时：
1. 验证旧Token有效性
2. 从旧Token提取用户信息
3. 生成新Token
4. （可选）将旧Token加入黑名单
```

### 多端登录控制
```java
// 限制用户同时在线设备数：
1. Redis存储 user_id -> [token1, token2...]
2. 登录时检查在线Token数量
3. 超过限制时踢掉最早的Token
```

