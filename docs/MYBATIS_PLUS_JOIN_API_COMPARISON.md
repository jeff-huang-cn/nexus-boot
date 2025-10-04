# MyBatis-Plus-Join API 对比说明

## 为什么 `MPJLambdaQueryWrapper` 不能用 join？

这是 MyBatis-Plus-Join 库中一个容易混淆的设计问题。

### 两个相似的类

```java
// ❌ MPJLambdaQueryWrapper - 名字有误导性
com.github.yulichang.query.MPJLambdaQueryWrapper

// ✅ MPJLambdaWrapper - 真正用于联表查询
com.github.yulichang.wrapper.MPJLambdaWrapper
```

### 类的定位对比

| 特性 | MPJLambdaQueryWrapper | MPJLambdaWrapper |
|------|----------------------|------------------|
| **包路径** | `com.github.yulichang.query` | `com.github.yulichang.wrapper` |
| **设计目的** | 普通查询 + 部分扩展功能 | 专为多表联查设计 |
| **是否支持 join** | ❌ **不支持** | ✅ 支持 |
| **join 方法** | 无 `innerJoin`、`leftJoin` 等 | 完整的 join 方法 |
| **推荐使用** | ❌ 不推荐用于联表 | ✅ **推荐** |
| **典型用途** | 单表查询扩展 | 多表联表查询 |

## 为什么会这样设计？

### 1. 历史原因

MyBatis-Plus-Join 在版本演进过程中进行了 API 重构：

- **早期版本（1.4.x 之前）**：可能只有一个 wrapper 类
- **新版本（1.5.x）**：重构后分为多个 wrapper，职责更明确
- **兼容性考虑**：保留旧类避免破坏现有代码

### 2. 职责分离

```java
// MPJLambdaQueryWrapper - 继承自 MyBatis-Plus 的查询构造器
// 主要用于单表查询的扩展，不涉及多表 join
public class MPJLambdaQueryWrapper<T> extends AbstractWrapper<T, ...> {
    // 没有 join 相关方法
}

// MPJLambdaWrapper - 专门的联表查询构造器
// 提供完整的多表 join 功能
public class MPJLambdaWrapper<T> extends AbstractWrapper<T, ...> {
    // ✅ 提供 innerJoin、leftJoin、rightJoin 等
    public MPJLambdaWrapper<T> leftJoin(...) { ... }
    public MPJLambdaWrapper<T> innerJoin(...) { ... }
    public MPJLambdaWrapper<T> rightJoin(...) { ... }
}
```

## 正确的使用方式

### ❌ 错误示例

```java
import com.github.yulichang.query.MPJLambdaQueryWrapper;  // ❌ 错误的导入

public List<RoleDO> getListByUserId(Long userId) {
    // ❌ MPJLambdaQueryWrapper 没有 innerJoin 方法！
    MPJLambdaQueryWrapper<RoleDO> wrapper = new MPJLambdaQueryWrapper<RoleDO>()
        .selectAll(RoleDO.class)
        .innerJoin(UserRoleDO.class, ...);  // ❌ 编译错误：找不到方法
    
    return roleMapper.selectJoinList(RoleDO.class, wrapper);
}
```

### ✅ 正确示例

```java
import com.github.yulichang.wrapper.MPJLambdaWrapper;  // ✅ 正确的导入

public List<RoleDO> getListByUserId(Long userId) {
    // ✅ MPJLambdaWrapper 支持 innerJoin
    MPJLambdaWrapper<RoleDO> wrapper = new MPJLambdaWrapper<RoleDO>()
        .selectAll(RoleDO.class)
        .innerJoin(UserRoleDO.class, UserRoleDO::getRoleId, RoleDO::getId)  // ✅ 正确
        .eq(UserRoleDO::getUserId, userId);
    
    return roleMapper.selectJoinList(RoleDO.class, wrapper);
}
```

## IDE 自动导入陷阱

### 问题场景

当你输入 `MPJLambdaQueryWrapper` 时，IDE 可能会自动导入错误的类：

```java
// IDE 自动导入（可能是错误的）
import com.github.yulichang.query.MPJLambdaQueryWrapper;  // ❌

// 然后你写代码时发现没有 join 方法
wrapper.innerJoin(...)  // ❌ 找不到方法
```

### 解决方案

1. **直接输入正确的类名**：`MPJLambdaWrapper`（不是 `MPJLambdaQueryWrapper`）
2. **检查导入语句**：确保是 `com.github.yulichang.wrapper.MPJLambdaWrapper`
3. **删除错误的导入**：如果已经导入了 `MPJLambdaQueryWrapper`，删除它

## 完整的最佳实践

### 必需的导入

```java
// Wrapper 类 - 用于构建联表查询
import com.github.yulichang.wrapper.MPJLambdaWrapper;

// Mapper 基类 - 提供 selectJoinList 等方法
import com.github.yulichang.base.MPJBaseMapper;
```

### Mapper 接口

```java
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoleMapper extends MPJBaseMapper<RoleDO> {
    // ✅ 继承 MPJBaseMapper，获得联表查询能力
}
```

### Service 实现

```java
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl implements RoleService {
    
    @Resource
    private RoleMapper roleMapper;
    
    @Override
    public List<RoleDO> getListByUserId(Long userId) {
        return roleMapper.selectJoinList(
            RoleDO.class,
            new MPJLambdaWrapper<RoleDO>()
                .selectAll(RoleDO.class)
                .innerJoin(UserRoleDO.class, UserRoleDO::getRoleId, RoleDO::getId)
                .eq(UserRoleDO::getUserId, userId)
        );
    }
}
```

## 版本说明

### MyBatis-Plus-Join 1.5.4（当前版本）

```xml
<dependency>
    <groupId>com.github.yulichang</groupId>
    <artifactId>mybatis-plus-join-boot-starter</artifactId>
    <version>1.5.4</version>
</dependency>
```

**推荐使用：**
- ✅ `MPJLambdaWrapper` - 联表查询
- ✅ `MPJBaseMapper` - Mapper 基类

**不推荐：**
- ❌ `MPJLambdaQueryWrapper` - 不支持 join，容易混淆

## 常见错误排查

### 错误 1：找不到 innerJoin 方法

```java
// 错误提示：Cannot resolve method 'innerJoin' in 'MPJLambdaQueryWrapper'

MPJLambdaQueryWrapper<RoleDO> wrapper = new MPJLambdaQueryWrapper<>();
wrapper.innerJoin(...);  // ❌ 找不到方法
```

**原因**：使用了 `MPJLambdaQueryWrapper` 而不是 `MPJLambdaWrapper`

**解决**：
```java
// 改为使用 MPJLambdaWrapper
MPJLambdaWrapper<RoleDO> wrapper = new MPJLambdaWrapper<>();
wrapper.innerJoin(...);  // ✅ 正确
```

### 错误 2：导入了错误的包

```java
import com.github.yulichang.query.MPJLambdaQueryWrapper;  // ❌ 错误

// 应该导入
import com.github.yulichang.wrapper.MPJLambdaWrapper;  // ✅ 正确
```

### 错误 3：Mapper 没有继承 MPJBaseMapper

```java
// ❌ 错误
public interface RoleMapper extends BaseMapper<RoleDO> {
}

// ✅ 正确
public interface RoleMapper extends MPJBaseMapper<RoleDO> {
}
```

## 快速记忆法

记住这个规则：

```
联表查询 = MPJLambdaWrapper + MPJBaseMapper
         └─ wrapper 包下     └─ base 包下
         └─ 不是 query 包！
```

## 总结

| 问题 | 答案 |
|------|------|
| **为什么 `MPJLambdaQueryWrapper` 不能用 join？** | 设计定位不同，它不是为联表查询设计的 |
| **应该用什么类做联表查询？** | `MPJLambdaWrapper` |
| **如何记住正确的类？** | 看包名：`wrapper` 包下的才能做 join |
| **Mapper 要继承什么？** | `MPJBaseMapper`（不是 `BaseMapper`） |
| **如何避免导入错误？** | 检查导入语句，确保是 `wrapper.MPJLambdaWrapper` |

## 参考链接

- [MyBatis-Plus-Join 官方文档](https://gitee.com/yulichang1/mybatis-plus-join)
- [MyBatis-Plus-Join GitHub](https://github.com/yulichang/mybatis-plus-join)

---

**记住：想用 join，就用 `MPJLambdaWrapper`！** 🎯

