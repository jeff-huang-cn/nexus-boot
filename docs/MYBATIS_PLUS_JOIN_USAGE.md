# MyBatis-Plus-Join 使用指南

## 问题描述

在使用 MyBatis-Plus-Join 进行联表查询时，提示找不到 `innerJoin` 方法。

```java
// ❌ 错误示例
MPJLambdaQueryWrapper<RoleDO> wrapper = new MPJLambdaQueryWrapper<RoleDO>()
    .selectAll(RoleDO.class)
    .innerJoin(UserRoleDO.class, UserRoleDO::getRoleId, RoleDO::getId)  // ❌ 找不到方法
    .eq(UserRoleDO::getUserId, userId);
return wrapper.selectAll(RoleDO.class);  // ❌ wrapper 没有 selectAll 方法
```

## 问题原因

1. **导入的包错误**：导入的是 `com.github.yulichang.query.MPJLambdaQueryWrapper`（旧版本 API），应该使用 `com.github.yulichang.wrapper.MPJLambdaWrapper`（新版本 API）
2. **Mapper 继承错误**：Mapper 继承的是 `BaseMapper`，而不是 `MPJBaseMapper`
3. **查询方法错误**：使用了错误的查询方法 `wrapper.selectAll()`

## 解决方案

### 1. 导入正确的包（重要！）

```java
// ❌ 错误：导入旧版本 API
import com.github.yulichang.query.MPJLambdaQueryWrapper;

// ✅ 正确：导入新版本 API (1.5.x)
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.github.yulichang.base.MPJBaseMapper;
```

### 2. Mapper 继承 `MPJBaseMapper`

```java
// ❌ 错误
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

@Mapper
public interface RoleMapper extends BaseMapper<RoleDO> {
}

// ✅ 正确
import com.github.yulichang.base.MPJBaseMapper;

@Mapper
public interface RoleMapper extends MPJBaseMapper<RoleDO> {
}
```

### 3. 使用正确的 Wrapper 类和查询方法

```java
// ✅ 完整正确示例
import com.github.yulichang.wrapper.MPJLambdaWrapper;

@Override
public List<RoleDO> getListByUserId(Long userId) {
    // 使用 MPJLambdaWrapper，不是 MPJLambdaQueryWrapper
    MPJLambdaWrapper<RoleDO> wrapper = new MPJLambdaWrapper<RoleDO>()
        .selectAll(RoleDO.class)
        .innerJoin(UserRoleDO.class, UserRoleDO::getRoleId, RoleDO::getId)
        .eq(UserRoleDO::getUserId, userId);
    
    // 使用 mapper.selectJoinList() 执行联表查询
    return roleMapper.selectJoinList(RoleDO.class, wrapper);
}
```

## MPJBaseMapper 常用方法

### 查询方法

```java
// 1. 联表查询列表
List<T> selectJoinList(Class<T> clazz, MPJLambdaWrapper<T> wrapper);

// 2. 联表查询单条
T selectJoinOne(Class<T> clazz, MPJLambdaWrapper<T> wrapper);

// 3. 联表查询分页
IPage<T> selectJoinPage(IPage<T> page, Class<T> clazz, MPJLambdaWrapper<T> wrapper);

// 4. 联表查询 Map 列表
List<Map<String, Object>> selectJoinMaps(MPJLambdaWrapper<?> wrapper);

// 5. 联表查询 Map 分页
IPage<Map<String, Object>> selectJoinMapsPage(IPage<?> page, MPJLambdaWrapper<?> wrapper);
```

## 联表查询示例

### 1. 基本 INNER JOIN

```java
// 查询用户的角色列表
@Override
public List<RoleDO> getListByUserId(Long userId) {
    return roleMapper.selectJoinList(
        RoleDO.class,
        new MPJLambdaWrapper<RoleDO>()
            .selectAll(RoleDO.class)  // 查询角色表所有字段
            .innerJoin(UserRoleDO.class, UserRoleDO::getRoleId, RoleDO::getId)
            .eq(UserRoleDO::getUserId, userId)
    );
}
```

### 2. LEFT JOIN

```java
// 左连接查询
List<UserDO> users = userMapper.selectJoinList(
    UserDO.class,
    new MPJLambdaWrapper<UserDO>()
        .selectAll(UserDO.class)
        .selectAs(DeptDO::getName, "deptName")  // 查询部门名称
        .leftJoin(DeptDO.class, DeptDO::getId, UserDO::getDeptId)
        .eq(UserDO::getStatus, 1)
);
```

### 3. 多表联查

```java
// 查询用户及其角色、部门信息
List<UserVO> users = userMapper.selectJoinList(
    UserVO.class,
    new MPJLambdaWrapper<UserDO>()
        .selectAll(UserDO.class)
        .selectCollection(RoleDO.class, UserVO::getRoles)  // 一对多
        .selectAs(DeptDO::getName, UserVO::getDeptName)
        .leftJoin(UserRoleDO.class, UserRoleDO::getUserId, UserDO::getId)
        .leftJoin(RoleDO.class, RoleDO::getId, UserRoleDO::getRoleId)
        .leftJoin(DeptDO.class, DeptDO::getId, UserDO::getDeptId)
        .eq(UserDO::getStatus, 1)
);
```

### 4. 分页查询

```java
// 联表分页查询
@Override
public IPage<RoleDO> getPageByUserId(Page<RoleDO> page, Long userId) {
    return roleMapper.selectJoinPage(
        page,
        RoleDO.class,
        new MPJLambdaWrapper<RoleDO>()
            .selectAll(RoleDO.class)
            .innerJoin(UserRoleDO.class, UserRoleDO::getRoleId, RoleDO::getId)
            .eq(UserRoleDO::getUserId, userId)
            .orderByDesc(RoleDO::getCreateTime)
    );
}
```

### 5. 自定义查询字段

```java
// 只查询指定字段
List<RoleVO> roles = roleMapper.selectJoinList(
    RoleVO.class,
    new MPJLambdaWrapper<RoleDO>()
        .select(RoleDO::getId, RoleDO::getName, RoleDO::getCode)
        .selectAs(UserDO::getUsername, RoleVO::getCreatorName)
        .leftJoin(UserDO.class, UserDO::getId, RoleDO::getCreator)
);
```

## 依赖配置

### Maven 依赖

```xml
<!-- MyBatis-Plus Join 扩展 -->
<dependency>
    <groupId>com.github.yulichang</groupId>
    <artifactId>mybatis-plus-join-boot-starter</artifactId>
    <version>1.5.4</version>
</dependency>
```

## 最佳实践

### 1. Mapper 统一继承

为了支持联表查询，建议所有 Mapper 统一继承 `MPJBaseMapper`：

```java
@Mapper
public interface UserMapper extends MPJBaseMapper<UserDO> {
}
```

**说明：**
- `MPJBaseMapper` 继承自 `BaseMapper`
- 包含 `BaseMapper` 的所有方法
- 额外提供联表查询方法
- 无需担心兼容性问题

### 2. 避免 N+1 查询

```java
// ❌ 不推荐：N+1 查询
List<UserDO> users = userMapper.selectList(null);
for (UserDO user : users) {
    user.setRoles(roleMapper.selectByUserId(user.getId())); // N次查询
}

// ✅ 推荐：一次联表查询
List<UserVO> users = userMapper.selectJoinList(
    UserVO.class,
    new MPJLambdaWrapper<UserDO>()
        .selectAll(UserDO.class)
        .selectCollection(RoleDO.class, UserVO::getRoles)
        .leftJoin(UserRoleDO.class, UserRoleDO::getUserId, UserDO::getId)
        .leftJoin(RoleDO.class, RoleDO::getId, UserRoleDO::getRoleId)
);
```

### 3. 合理使用 selectAll vs select

```java
// selectAll：查询主表所有字段（推荐用于单表或主表字段不多的情况）
.selectAll(RoleDO.class)

// select：只查询需要的字段（推荐用于多表联查或字段较多的情况）
.select(RoleDO::getId, RoleDO::getName, RoleDO::getCode)
.selectAs(UserDO::getUsername, RoleVO::getCreatorName)
```

### 4. 使用别名避免字段冲突

```java
// 多表有同名字段时使用别名
new MPJLambdaWrapper<UserDO>()
    .select(UserDO::getId, UserDO::getName)
    .selectAs(DeptDO::getId, UserVO::getDeptId)    // 使用别名
    .selectAs(DeptDO::getName, UserVO::getDeptName)
    .leftJoin(DeptDO.class, DeptDO::getId, UserDO::getDeptId)
```

## 常见问题

### Q1: 为什么要继承 `MPJBaseMapper` 而不是 `BaseMapper`？

**A:** `MPJBaseMapper` 继承自 `BaseMapper`，在保留所有 MyBatis-Plus 功能的基础上，额外提供了联表查询的能力：

```java
public interface MPJBaseMapper<T> extends BaseMapper<T> {
    // 额外的联表查询方法
    List<T> selectJoinList(Class<T> clazz, MPJLambdaWrapper<T> wrapper);
    // ... 其他方法
}
```

### Q2: `MPJBaseMapper` 会影响现有的 MyBatis-Plus 功能吗？

**A:** 不会。`MPJBaseMapper` 完全兼容 `BaseMapper`，所有原有的方法都可以正常使用。

### Q3: 为什么提示找不到 `innerJoin` 方法？

**A:** 可能的原因：
1. **导入的包不对**：确保导入的是 `com.github.yulichang.wrapper.MPJLambdaWrapper`，而不是 `com.github.yulichang.query.MPJLambdaQueryWrapper`（旧版本）
2. **Mapper 没有继承 `MPJBaseMapper`**：必须继承 `MPJBaseMapper` 才能使用联表查询
3. **版本问题**：确保使用的是 mybatis-plus-join 1.5.x 版本

```java
// ✅ 正确的导入
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.github.yulichang.base.MPJBaseMapper;

// ❌ 错误的导入（旧版本）
import com.github.yulichang.query.MPJLambdaQueryWrapper;
```

### Q4: 如何进行复杂的多表联查？

**A:** 使用链式调用，支持多个 JOIN：

```java
new MPJLambdaWrapper<UserDO>()
    .selectAll(UserDO.class)
    .leftJoin(DeptDO.class, DeptDO::getId, UserDO::getDeptId)
    .leftJoin(UserRoleDO.class, UserRoleDO::getUserId, UserDO::getId)
    .leftJoin(RoleDO.class, RoleDO::getId, UserRoleDO::getRoleId)
    .eq(UserDO::getStatus, 1)
```

### Q5: 联表查询的性能如何？

**A:** 
- 联表查询会生成标准的 SQL JOIN 语句
- 性能与手写 SQL 基本一致
- 相比 N+1 查询有显著提升
- 建议在关联字段上建立索引

### Q6: 可以和 MyBatis-Plus 的其他功能一起使用吗？

**A:** 可以。包括：
- 分页插件：`Page<T>`
- 条件构造器：`LambdaQueryWrapper`
- 逻辑删除
- 自动填充
- 乐观锁
等功能都可以正常使用。

## 参考资源

- [MyBatis-Plus-Join 官方文档](https://gitee.com/yulichang1/mybatis-plus-join)
- [MyBatis-Plus 官方文档](https://baomidou.com/)

## 更新记录

- 2025-01-04: 创建文档，记录 MPJLambdaQueryWrapper 使用方法和常见问题

