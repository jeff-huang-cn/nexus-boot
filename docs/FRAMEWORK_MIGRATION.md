# Framework 通用组件迁移总结

## 📦 迁移概述

将 `nexus-backend-admin` 模块中的通用基础设施代码迁移到 `nexus-framework` 模块，以实现代码复用和模块化设计。

**迁移日期**：2025-10-03

---

## ✅ 已迁移的组件

### 1. TreeUtils - 树形结构工具类

**原路径**：`com.nexus.backend.admin.common.util.TreeUtils`  
**新路径**：`com.nexus.framework.utils.tree.TreeUtils`

**功能**：
- ✅ `buildTree()` - 构建树形结构（通用方法）
- ✅ `traverseTree()` - 遍历树形结构（深度优先）
- ✅ `findNode()` - 在树中查找节点
- ✅ `flattenTree()` - 将树形结构扁平化为列表
- ✅ `filterTree()` - 过滤树形结构

**使用示例**：
```java
import com.nexus.framework.utils.tree.TreeUtils;

// 构建菜单树
List<MenuRespVO> menuTree = TreeUtils.buildTree(
    menuVOList,
    MenuRespVO::getId,
    MenuRespVO::getParentId,
    MenuRespVO::setChildren,
    0L
);
```

---

### 2. Result - 统一响应结果

**原路径**：`com.nexus.backend.admin.common.result.Result`（已删除）  
**新路径**：`com.nexus.framework.web.result.Result`（framework 原有）

**说明**：
- framework 已经有 `Result` 类
- 删除了 admin 模块中的重复定义
- 所有 Controller 现在统一使用 framework 的 `Result`

**使用示例**：
```java
import com.nexus.framework.web.result.Result;

@GetMapping("/list")
public Result<List<UserRespVO>> getList() {
    return Result.success(userList);
}
```

---

### 3. BusinessException - 业务异常

**原路径**：`com.nexus.backend.admin.common.exception.BusinessException`  
**新路径**：`com.nexus.framework.web.exception.BusinessException`

**功能**：
- 业务异常基类
- 支持自定义错误码和错误消息
- 继承自 `RuntimeException`

**使用示例**：
```java
import com.nexus.framework.web.exception.BusinessException;

if (user == null) {
    throw new BusinessException("用户不存在");
}

// 或指定错误码
throw new BusinessException(404, "资源不存在");
```

---

### 4. GlobalExceptionHandler - 全局异常处理器

**原路径**：`com.nexus.backend.admin.common.exception.GlobalExceptionHandler`  
**新路径**：`com.nexus.framework.web.exception.GlobalExceptionHandler`

**功能**：
- 统一处理业务异常 `BusinessException`
- 参数校验异常处理（`@Valid`、`@RequestBody`、`@RequestParam`）
- 系统异常统一处理

**异常处理列表**：
| 异常类型 | HTTP状态码 | 处理方法 |
|---------|-----------|---------|
| `BusinessException` | 200 | `handleBusinessException` |
| `MethodArgumentNotValidException` | 400 | `handleMethodArgumentNotValidException` |
| `BindException` | 400 | `handleBindException` |
| `ConstraintViolationException` | 400 | `handleConstraintViolationException` |
| `Exception` | 500 | `handleException` |

---

## 🔧 Framework 依赖更新

在 `nexus-framework/pom.xml` 中添加了 Spring Web 依赖（设置为 optional）：

```xml
<!-- Spring Boot Web Starter (可选，用于 GlobalExceptionHandler) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <optional>true</optional>
</dependency>
```

**说明**：
- `optional=true` 表示这是可选依赖
- Framework 可以提供异常处理器，但不会强制所有使用 framework 的模块都依赖 Web
- 实际使用 `GlobalExceptionHandler` 的模块（如 admin）需要自己依赖 `spring-boot-starter-web`

---

## 📝 Admin 模块更新

### 已删除的文件

1. `com.nexus.backend.admin.common.result.Result`
2. `com.nexus.backend.admin.common.util.TreeUtils`
3. `com.nexus.backend.admin.common.exception.BusinessException`
4. `com.nexus.backend.admin.common.exception.GlobalExceptionHandler`

### 已更新的文件

所有引用旧路径的文件都已更新为新路径：

**Controllers**:
- ✅ `MenuController.java` - 更新为使用 `TreeUtils` 和 `Result`
- ✅ `RoleController.java` - 更新为使用 `Result`
- ✅ `DictController.java` - 更新为使用 `Result`

**Services**:
- ✅ `CodegenServiceImpl.java` - 更新为使用 `BusinessException`
- ✅ `DatabaseTableServiceImpl.java` - 更新为使用 `BusinessException`

**Service Interfaces**:
- ✅ `MenuService.java` - 移除了 `buildTree()` 静态方法（现在使用 `TreeUtils`）

---

## 🎯 使用指南

### 1. 在其他模块中使用 TreeUtils

```java
// 1. 在任何 Service 中使用
@Service
public class DeptServiceImpl implements DeptService {
    
    @Override
    public List<DeptRespVO> getDeptTree() {
        // 获取部门列表
        List<DeptDO> deptList = deptMapper.selectList(...);
        
        // 转换为 VO
        List<DeptRespVO> deptVOList = convert(deptList);
        
        // 使用 TreeUtils 构建树
        return TreeUtils.buildTree(
            deptVOList,
            DeptRespVO::getId,
            DeptRespVO::getParentId,
            DeptRespVO::setChildren,
            0L
        );
    }
}
```

### 2. 在 Controller 中使用 Result

```java
import com.nexus.framework.web.result.Result;

@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @GetMapping("/{id}")
    public Result<UserRespVO> getById(@PathVariable Long id) {
        UserDO user = userService.getById(id);
        UserRespVO vo = BeanUtil.copyProperties(user, UserRespVO.class);
        return Result.success(vo);
    }
}
```

### 3. 抛出业务异常

```java
import com.nexus.framework.web.exception.BusinessException;

@Service
public class UserServiceImpl implements UserService {
    
    public void deleteUser(Long id) {
        UserDO user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getIsSystem()) {
            throw new BusinessException(403, "系统用户不能删除");
        }
        userMapper.deleteById(id);
    }
}
```

---

## 📚 相关文档

- [树形结构工具使用说明](../nexus-backend-admin/docs/TREE_UTIL_USAGE.md)
- [Service 层设计规范](../nexus-backend-admin/docs/SERVICE_RULE_CHECKLIST.md)

---

## ⚠️ 注意事项

### 1. IDE 刷新

迁移完成后，需要在 IDE 中刷新 Maven 依赖：
- **IDEA**：右键项目 → Maven → Reload Project
- **Eclipse**：右键项目 → Maven → Update Project

### 2. 依赖关系

- `nexus-backend-admin` 已经依赖 `nexus-framework`
- 新建的业务模块也需要添加 `nexus-framework` 依赖

### 3. 编译顺序

编译时先编译 framework 模块：
```bash
cd nexus-boot/nexus-framework
mvn clean install
```

然后编译 admin 模块：
```bash
cd ../nexus-backend-admin
mvn clean install
```

---

## 🚀 后续优化建议

### 1. 添加更多通用工具

可以考虑将以下组件也迁移到 framework：
- 分页工具类
- 日期时间工具类
- 加密解密工具类
- 文件上传工具类

### 2. 创建独立的 starter 模块

```
nexus-framework/
├── nexus-framework-core/        # 核心工具类
├── nexus-framework-web/         # Web 相关（Result、异常处理）
├── nexus-framework-mybatis/     # MyBatis 扩展
└── nexus-framework-redis/       # Redis 扩展
```

### 3. 完善文档

- 为每个工具类添加详细的使用文档
- 提供更多使用示例
- 创建最佳实践指南

---

**迁移完成时间**：2025-10-03  
**迁移人员**：nexus  
**影响范围**：所有使用 admin 模块通用组件的代码

