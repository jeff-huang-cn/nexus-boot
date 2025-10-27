# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

nexus-boot 是一个企业级快速开发框架,基于 **Spring Boot 3.5.6 + React 19.x**,采用前后端分离架构,支持多租户、代码生成、RBAC 权限管理等企业级功能。

### 技术栈
- **后端**: Spring Boot 3.5.6, Java 17, MyBatis-Plus 3.5.12, Spring Security, JWT
- **前端**: React 19.x, TypeScript 4.9.5, Ant Design 5.x, React Router 7.x, Axios 1.12.2
- **数据库**: MySQL + Redis (Redisson)
- **代码生成**: Velocity 2.3 模板引擎
- **工具库**: Hutool, Guava, EasyExcel, MapStruct

## 项目结构

```
nexus-boot/                          # 父 pom (多模块 Maven 项目)
├── nexus-framework/                 # 共享框架模块 (JAR)
│   ├── security/                    # JWT 认证、权限管理
│   ├── tenant/                      # 多租户支持 (ThreadLocal 隔离)
│   ├── mybatis/                     # MyBatis-Plus 配置、基础实体
│   ├── web/                         # 统一响应、全局异常处理
│   ├── excel/                       # EasyExcel 工具
│   └── utils/                       # 通用工具类
└── nexus-backend-admin/             # 后台管理应用 (Spring Boot)
    ├── src/main/java/com/nexus/backend/admin/
    │   ├── BackendAdminApplication.java  # 启动类
    │   ├── controller/              # 控制器层
    │   │   ├── auth/                # 认证 (登录、登出)
    │   │   ├── user/                # 用户管理
    │   │   ├── permission/          # 权限管理 (用户、角色、菜单)
    │   │   ├── tenant/              # 租户管理
    │   │   ├── dict/                # 数据字典
    │   │   └── codegen/             # 代码生成
    │   ├── service/                 # 业务逻辑层
    │   ├── dal/                     # 数据访问层
    │   │   ├── dataobject/          # 数据库实体 (DO)
    │   │   └── mapper/              # MyBatis Mapper
    │   └── convert/                 # VO <-> DO 转换 (MapStruct)
    ├── src/main/resources/
    │   ├── application.yaml         # 主配置文件
    │   └── templates/               # Velocity 代码生成模板
    └── ui/                          # React 前端项目
        ├── src/
        │   ├── pages/               # 页面组件
        │   ├── services/            # API 调用层
        │   ├── components/          # 通用组件
        │   ├── contexts/            # React Context (认证、菜单)
        │   ├── utils/               # 工具函数
        │   └── types/               # TypeScript 类型定义
        └── package.json
```

## 常用命令

### 后端开发

```bash
# 编译整个项目
mvn clean install

# 编译单个模块
cd nexus-backend-admin
mvn clean install

# 运行后台管理应用 (启动后访问 http://localhost:8080)
cd nexus-backend-admin
mvn spring-boot:run

# 或直接运行主类
# com.nexus.backend.admin.BackendAdminApplication

# 跳过测试编译
mvn clean install -DskipTests

# 清理并重新编译 nexus-framework
cd nexus-framework
mvn clean install

# 生成可执行 JAR
cd nexus-backend-admin
mvn clean package
# 生成的 JAR 在 target/nexus-backend-admin-0.0.1-SNAPSHOT.jar
```

### 前端开发

```bash
# 进入前端目录
cd nexus-backend-admin/ui

# 安装依赖
npm install

# 启动开发服务器 (默认端口 3000)
npm start

# 构建生产版本
npm run build

# 运行测试
npm test
```

### 数据库初始化

1. 创建数据库 `codegen_admin`
2. 执行 SQL 脚本初始化表结构和数据 (如有提供)
3. 修改 `application.yaml` 中的数据库连接信息:
   ```yaml
   spring.datasource.dynamic.datasource.master.url
   spring.datasource.dynamic.datasource.master.username
   spring.datasource.dynamic.datasource.master.password
   ```

## 核心架构

### 1. 分层架构

```
Controller (API 层)
    ↓
Service (业务逻辑层)
    ↓
Mapper (数据访问层)
    ↓
Database (MySQL)
```

**命名约定**:
- `DO` (Data Object): 数据库实体,继承 `BaseDO` / `TenantBaseCreateDO`
- `ReqVO` (Request Value Object): 请求参数对象
  - `PageReqVO`: 分页查询请求
  - `SaveReqVO`: 新增/更新请求
- `RespVO` (Response Value Object): 响应对象
- `Mapper`: MyBatis 接口,继承 `BaseMapper<T>` / `MPJBaseMapper<T>` (支持多表联查)
- `Service`: 业务接口
- `ServiceImpl`: 业务实现类,继承 `ServiceImpl<Mapper, DO>`

### 2. 统一响应格式

所有 API 返回格式为 `Result<T>`:

```java
{
  "code": 200,          // 200=成功, 其他=失败
  "message": "操作成功",
  "data": { ... },      // 业务数据
  "timestamp": 1234567890
}

// 分页响应
{
  "code": 200,
  "data": {
    "records": [...],   // 数据列表
    "total": 100        // 总记录数
  }
}
```

### 3. JWT 认证流程

1. **登录**: `POST /admin/auth/login` 获取 JWT token
2. **请求**: 在请求头中添加 `Authorization: Bearer <token>`
3. **权限校验**: 使用 `@PreAuthorize("@ss.hasPermission('system:user:query')")` 注解
4. **登出**: `POST /admin/auth/logout` 将 token 加入黑名单

**关键类**:
- `JwtAuthenticationFilter`: JWT 认证过滤器
- `JwtTokenGenerator`: JWT 生成器 (RSA256 签名)
- `SecurityContextUtils`: 获取当前登录用户信息
- `PermissionLoader`: 权限加载器 (从数据库加载用户权限)

### 4. 多租户机制

**租户隔离方式**: 基于数据库表的 `tenant_id` 字段

**关键类**:
- `TenantContextHolder`: 租户上下文 (ThreadLocal 存储当前租户 ID)
- `TenantBaseCreateDO`: 租户实体基类 (包含 `tenantId` 字段)

**使用方式**:
```java
// 设置租户 ID
TenantContextHolder.setTenantId(1L);

// 忽略租户过滤 (查询全局数据)
TenantContextHolder.setIgnore(true);
try {
    // 执行查询...
} finally {
    TenantContextHolder.setIgnore(false);
}
```

### 5. 代码生成器

**使用流程**:

1. **配置数据源**: 通过 `/admin/codegen/datasource/create` 添加数据源
2. **导入表**: 通过 `/admin/codegen/importTables` 导入数据库表
3. **配置表字段**: 编辑字段映射、查询条件、HTML 类型等
4. **生成代码**: 调用 `/admin/codegen/generate` 生成 ZIP 压缩包

**生成内容**:
- **后端**: Controller, Service, Mapper, DO, VO, Converter (Java)
- **前端**: 页面组件, API 调用, 类型定义 (React + TypeScript)

**模板路径**: `src/main/resources/templates/`
- `java/`: 后端模板 (controller.vm, service.vm, mapper.vm 等)
- `react/`: 前端模板 (crud/, tree/, sub/)

**模板引擎**: Velocity 2.3

### 6. 权限系统

**RBAC 模型** (Role-Based Access Control):

```
用户 (system_user)
  ↓ N:N
角色 (system_role)
  ↓ N:N
菜单/权限 (system_menu)
```

**权限标识格式**: `system:user:query` (模块:资源:操作)

**菜单类型**:
- `1`: 目录 (Directory)
- `2`: 菜单 (Menu)
- `3`: 按钮 (Button)

**权限校验**:
```java
@PreAuthorize("@ss.hasPermission('system:user:create')")
public Result<Long> createUser(@RequestBody UserSaveReqVO reqVO) {
    // ...
}
```

### 7. MyBatis-Plus 配置

**逻辑删除**:
- 字段: `deleted` (0=未删除, 1=已删除)
- 查询时自动过滤已删除记录

**审计字段** (自动填充):
- `create_time`: 创建时间
- `update_time`: 更新时间
- `creator`: 创建人
- `updater`: 更新人

**分页插件**: `PaginationInnerInterceptor` (支持 MySQL)

**联表查询**: 使用 `MPJBaseMapper<T>` + `MPJLambdaWrapper`

### 8. 异常处理

**全局异常处理器**: `GlobalExceptionHandler`

**业务异常**:
```java
throw new BusinessException("用户不存在");
```

**参数校验异常**: 使用 `@Valid` + JSR-380 注解
```java
@NotBlank(message = "用户名不能为空")
private String username;
```

## 开发规范

### 后端开发

1. **控制器方法命名**:
   - `create*`: 新增
   - `update*`: 更新
   - `delete*`: 删除
   - `get*`: 获取单个
   - `list*`: 获取列表
   - `page*`: 分页查询

2. **事务管理**: 在 Service 层方法上添加 `@Transactional`

3. **参数校验**: 使用 `@Valid` + 校验注解 (`@NotNull`, `@NotBlank`, `@Size`, `@Email` 等)

4. **日志记录**: 使用 Lombok 的 `@Slf4j` 注解
   ```java
   log.info("用户登录: username={}", username);
   log.error("操作失败", e);
   ```

5. **转换对象**: 使用 MapStruct 定义 Convert 接口
   ```java
   @Mapper
   public interface UserConvert {
       UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);
       UserRespVO convert(UserDO bean);
   }
   ```

### 前端开发

1. **API 调用**: 统一在 `services/` 目录下定义
   ```typescript
   export const getUserList = (params: UserPageReqVO): Promise<PageResult<UserRespVO>> => {
     return request.post('/admin/user/page', params);
   };
   ```

2. **类型定义**: 在 `types/` 目录下定义 TypeScript 接口
   ```typescript
   export interface UserRespVO {
     id: number;
     username: string;
     // ...
   }
   ```

3. **路由守卫**: 使用 `RouteGuard` 组件保护需要登录的页面

4. **全局消息**: 使用 `globalMessage` 工具显示提示
   ```typescript
   import { globalMessage } from '@/utils/globalMessage';
   globalMessage.success('操作成功');
   globalMessage.error('操作失败');
   ```

5. **认证 Token**: 使用 `auth.ts` 工具管理 Token
   ```typescript
   import { getToken, setToken, removeToken } from '@/utils/auth';
   ```

## 重要配置文件

### application.yaml
- **端口**: `server.port: 8080`
- **数据库**: `spring.datasource.dynamic.datasource.master.*`
- **Redis**: `spring.data.redis.*`
- **MyBatis-Plus**: `mybatis-plus.*`
- **JWT**: `nexus.security.jwk.*`
  - `validity-days: 90` (密钥有效期)
  - `auto-rotation-enabled: true` (自动轮换)

### package.json (前端)
- **启动**: `npm start` (端口 3000)
- **构建**: `npm run build`
- **依赖**:
  - React 19.x
  - Ant Design 5.x
  - Axios 1.12.2
  - React Router 7.x

## 调试技巧

### 后端调试

1. **SQL 日志**: `application.yaml` 中已启用 SQL 日志输出
   ```yaml
   mybatis-plus:
     configuration:
       log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
   ```

2. **调试日志级别**:
   ```yaml
   logging:
     level:
       com.nexus: DEBUG
   ```

3. **JWT 解析测试**: 可以使用 https://jwt.io 解析 token 内容

### 前端调试

1. **API 代理**: 前端开发时需要配置代理到后端 (修改 `package.json` 或创建 `setupProxy.js`)
2. **React DevTools**: 安装浏览器扩展调试组件状态
3. **Network 面板**: 查看 API 请求和响应

## 常见问题

### 1. 前端请求后端 API 跨域问题
- 开发环境: 配置代理 (在 `src/setupProxy.js` 中配置)
- 生产环境: 后端配置 CORS (已在 `WebConfig` 中配置)

### 2. JWT Token 过期
- Token 有效期默认 1 小时 (可在 `JwtTokenGenerator` 中调整)
- 前端需要实现 token 刷新机制或跳转登录页

### 3. MyBatis-Plus 代码生成器找不到表
- 检查数据源配置是否正确
- 确保数据库连接正常
- 检查表是否存在

### 4. 权限校验不生效
- 检查是否添加 `@PreAuthorize` 注解
- 确认 `PermissionLoader` 实现正确
- 查看 Spring Security 日志 (设置为 DEBUG 级别)

### 5. 多租户数据隔离问题
- 确保实体类继承 `TenantBaseCreateDO`
- 检查 `TenantContextHolder` 是否正确设置租户 ID
- 使用 `@DS("master")` 切换数据源时注意租户上下文

## 项目扩展

### 添加新模块

1. 在父 pom 中添加 `<module>` 声明
2. 创建模块目录和 pom.xml
3. 在 `dependencyManagement` 中添加依赖

### 添加新功能模块

1. 创建数据库表
2. 使用代码生成器生成基础代码
3. 调整生成的代码以满足业务需求
4. 添加权限配置到 `system_menu` 表
5. 编写单元测试

### 自定义代码模板

1. 修改 `src/main/resources/templates/` 下的 `.vm` 文件
2. 模板变量参考 `TemplateContext` 类
3. 重新运行代码生成器测试模板

## 相关资源

- **Spring Boot 文档**: https://spring.io/projects/spring-boot
- **MyBatis-Plus 文档**: https://baomidou.com/
- **React 文档**: https://react.dev/
- **Ant Design 文档**: https://ant.design/
- **Hutool 文档**: https://hutool.cn/