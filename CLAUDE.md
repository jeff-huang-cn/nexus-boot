# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.




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


```bash
mvn clean install

```


```bash
cd nexus-backend-admin/ui

npm install

npm start


npm test
```





















```yaml
```















