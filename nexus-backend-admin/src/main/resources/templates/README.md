# 代码生成模板结构说明

本目录包含用于代码生成的 Velocity 模板文件，按照标准的分层架构组织。

## 📁 目录结构

```
templates/
├── java/                           # Java 后端代码模板
│   ├── controller/                 # 控制器层
│   │   ├── controller.vm          # Controller 类模板
│   │   └── vo/                    # 视图对象
│   │       ├── PageReqVO.vm       # 分页查询请求 VO
│   │       ├── SaveReqVO.vm       # 保存（新增/编辑）请求 VO
│   │       └── RespVO.vm          # 响应 VO
│   ├── service/                   # 服务层
│   │   ├── service.vm             # Service 接口模板
│   │   └── serviceImpl.vm         # ServiceImpl 实现类模板
│   └── dal/                       # 数据访问层
│       ├── entity/                # 实体对象
│       │   └── do.vm              # Data Object 实体模板
│       └── mapper/                # MyBatis Mapper
│           ├── mapper.vm          # Mapper 接口模板
│           └── mapper.xml.vm      # MyBatis XML 映射文件模板
└── react/                         # React 前端代码模板
    ├── List.tsx.vm                # 列表页面组件
    ├── Form.tsx.vm                # 表单组件
    ├── api.ts.vm                  # API 调用服务
    └── types.ts.vm                # TypeScript 类型定义
```

## 🎯 模板说明

### Java 后端模板

#### Controller 层
- **controller.vm**: RESTful API 控制器，使用 VO 对象进行数据传输
- **PageReqVO.vm**: 分页查询请求对象，继承 `BasePageQuery`
- **SaveReqVO.vm**: 新增/编辑请求对象，包含 ID 字段（用于更新）和表单验证
- **RespVO.vm**: 响应对象，用于返回数据给前端

#### Service 层
- **service.vm**: 服务接口定义，简洁的方法命名（create/update/delete/getById/getPage/getList）
- **serviceImpl.vm**: 服务实现，包含完整的 CRUD 逻辑、VO 转换、动态查询条件

#### DAL 数据访问层
- **entity/do.vm**: 数据对象（DO），继承 `BaseDO`（包含审计字段）
- **mapper/mapper.vm**: MyBatis-Plus Mapper 接口
- **mapper/mapper.xml.vm**: MyBatis XML 映射文件

### React 前端模板

- **List.tsx.vm**: 列表页面，包含搜索、分页、新增、编辑、删除功能
- **Form.tsx.vm**: 表单组件，支持新增和编辑，动态生成表单项
- **api.ts.vm**: API 调用服务封装
- **types.ts.vm**: TypeScript 类型定义

## 🔧 使用的模板引擎

- **Velocity**: Apache Velocity 模板引擎
- **版本**: 2.3

## 📝 模板变量

### 通用变量
- `${packageName}`: 基础包名
- `${moduleName}`: 模块名
- `${businessName}`: 业务名
- `${className}`: 类名
- `${classNameFirstLower}`: 类名（首字母小写）
- `${classComment}`: 类注释
- `${author}`: 作者

### 字段相关
- `${columns}`: 所有列集合
- `${createColumns}`: 可用于新增的列
- `${updateColumns}`: 可用于编辑的列
- `${listColumns}`: 可用于列表查询的列
- `${listResultColumns}`: 可用于列表展示的列

### 列对象属性
- `columnName`: 数据库列名
- `columnComment`: 列注释
- `javaType`: Java 类型
- `javaField`: Java 字段名
- `primaryKey`: 是否主键
- `nullable`: 是否可为空
- `createOperation`: 是否用于新增
- `updateOperation`: 是否用于编辑
- `listOperation`: 是否用于查询
- `listOperationResult`: 是否展示在列表
- `listOperationCondition`: 查询条件类型（EQ/LIKE/BETWEEN等）
- `htmlType`: HTML 控件类型

## 🚀 生成的代码结构

### 📦 后端代码（树形展示）
```
backend/
├── controller/                   # 控制器层
│   └── {businessName}/
│       ├── {ClassName}Controller.java
│       │   package: com.nexus.backend.admin.controller.{businessName}
│       └── vo/
│           ├── {ClassName}PageReqVO.java
│           ├── {ClassName}SaveReqVO.java  # 包含ID字段用于更新
│           └── {ClassName}RespVO.java
│               package: com.nexus.backend.admin.controller.{businessName}.vo
├── service/                      # 服务层
│   └── {businessName}/
│       ├── {ClassName}Service.java
│       │   package: com.nexus.backend.admin.service.{businessName}
│       └── impl/
│           └── {ClassName}ServiceImpl.java
│               package: com.nexus.backend.admin.service.{businessName}.impl
├── dal/                         # 数据访问层
│   ├── dataobject/
│   │   └── {businessName}/
│   │       └── {ClassName}DO.java  # 继承 BaseDO
│   │           package: com.nexus.backend.admin.dal.dataobject.{businessName}
│   └── mapper/
│       └── {businessName}/
│           └── {ClassName}Mapper.java
│               package: com.nexus.backend.admin.dal.mapper.{businessName}
└── resources/                   # 配置文件
    └── mapper/
        └── {businessName}/
            └── {ClassName}Mapper.xml
```

### 🎨 前端代码（树形展示）
```
frontend/
├── pages/                       # 页面目录
│   └── {businessName}/
│       ├── {ClassName}List.tsx  # 列表页面
│       └── {ClassName}Form.tsx  # 表单页面（业务组件直接放在页面目录）
├── services/                    # API 服务目录
│   └── {businessName}.ts
└── types/                       # 类型定义目录
    └── {businessName}.ts
```

### 📂 实际项目路径映射

**后端代码**：
- `backend/controller/` → `src/main/java/{packageName}/controller/`
- `backend/service/` → `src/main/java/{packageName}/service/`
- `backend/dal/` → `src/main/java/{packageName}/dal/`
- `backend/resources/mapper/` → `src/main/resources/mapper/`

**前端代码**：
- `frontend/pages/{businessName}/` → `src/pages/{businessName}/`
- `frontend/services/{businessName}.ts` → `src/services/{businessName}.ts`
- `frontend/types/{businessName}.ts` → `src/types/{businessName}.ts`

## 📚 参考

本模板结构参考了 yudao 项目的最佳实践，采用标准的三层架构设计。

