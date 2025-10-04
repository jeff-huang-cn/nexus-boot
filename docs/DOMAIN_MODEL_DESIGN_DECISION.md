# 领域模型设计决策：充血模型 vs 贫血模型

## 💡 问题背景

在实现用户状态判断功能时，面临一个设计选择：

1. **贫血模型**：DO 只包含数据字段，Service 提供静态方法判断
2. **充血模型**：DO 中直接添加判断方法
3. **扩展类**：创建 UserBO 等业务对象包装 UserDO

我们最终选择了**充血模型（方案2）**。

## 📊 方案对比

### 方案1：贫血模型 + Service 静态方法

#### 代码示例

```java
// UserDO - 只有数据字段
@Data
public class UserDO {
    private Integer status;
    private LocalDateTime expiredTime;
    private LocalDateTime lockedTime;
    // ...
}

// UserService - 提供静态方法
public interface UserService {
    static boolean isEnabled(UserDO user) {
        if (user == null) return false;
        return user.getStatus() != null && user.getStatus() == 0;
    }
    
    static boolean isAccountNonExpired(UserDO user) {
        if (user == null) return false;
        // ...
    }
}

// 使用
UserDO user = userService.getUserByUsername("admin");
if (UserService.isEnabled(user)) {  // ❌ 调用不够直观
    // ...
}
```

#### 优点
- ✅ DO 保持简单，只有数据字段
- ✅ 符合传统三层架构
- ✅ DO 易于序列化和持久化

#### 缺点
- ❌ 调用不够自然：`UserService.isEnabled(user)` vs `user.isEnabled()`
- ❌ 静态方法无法被子类重写
- ❌ 违反面向对象封装原则（数据和行为分离）
- ❌ 需要在每个静态方法中判空

### 方案2：充血模型 - DO 中添加实例方法（✅ 推荐）

#### 代码示例

```java
// UserDO - 包含数据字段和业务方法
@Data
public class UserDO {
    private Integer status;
    private LocalDateTime expiredTime;
    private LocalDateTime lockedTime;
    
    // ✅ 业务方法
    public boolean isEnabled() {
        return this.status != null && this.status == 0;
    }
    
    public boolean isAccountNonExpired() {
        if (this.expiredTime == null) return true;
        LocalDateTime now = LocalDateTime.now();
        return now.isBefore(this.expiredTime) || now.isEqual(this.expiredTime);
    }
    
    public boolean isAccountNonLocked() {
        if (this.lockedTime == null) return true;
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(this.lockedTime) || now.isEqual(this.lockedTime);
    }
    
    public boolean isAccountAvailable() {
        return isEnabled() && isAccountNonExpired() && isAccountNonLocked();
    }
    
    public void lockAccount(int durationMinutes) {
        this.lockedTime = LocalDateTime.now().plusMinutes(durationMinutes);
    }
}

// 使用
UserDO user = userService.getUserByUsername("admin");
if (user.isEnabled()) {  // ✅ 调用非常直观
    // ...
}

if (!user.isAccountAvailable()) {
    throw new BusinessException(user.getUnavailableReason());
}

// 锁定账户1小时
user.lockAccount(60);
userService.update(user);
```

#### 优点
- ✅ **调用自然**：`user.isEnabled()` 符合面向对象思维
- ✅ **封装性好**：数据和行为在一起
- ✅ **不影响持久化**：MyBatis-Plus 只序列化 `@TableField` 标注的字段
- ✅ **无需判空**：对象本身不为空，方法内部处理字段判空
- ✅ **易于测试**：可以直接创建对象测试
- ✅ **符合 Spring Security 设计**：`UserDetails` 接口也是这样设计的
- ✅ **链式调用**：可以写出更优雅的代码

#### 缺点
- ⚠️ DO 不再是纯数据对象（但这不是问题）
- ⚠️ 需要理解充血模型概念

### 方案3：扩展类 - 创建 UserBO

#### 代码示例

```java
// UserDO - 纯数据对象
@Data
public class UserDO {
    private Integer status;
    private LocalDateTime expiredTime;
    // ...
}

// UserBO - 业务对象（包装 UserDO）
public class UserBO {
    private UserDO userDO;
    
    public UserBO(UserDO userDO) {
        this.userDO = userDO;
    }
    
    public boolean isEnabled() {
        return userDO.getStatus() != null && userDO.getStatus() == 0;
    }
    
    // ...
}

// 使用
UserDO user = userService.getUserByUsername("admin");
UserBO userBO = new UserBO(user);  // ❌ 需要额外转换
if (userBO.isEnabled()) {
    // ...
}
```

#### 优点
- ✅ DO 保持纯净
- ✅ 职责分离清晰

#### 缺点
- ❌ 需要额外的转换步骤
- ❌ 增加代码复杂度
- ❌ 多一个对象类
- ❌ 在每个使用处都需要包装

## 🎯 最终决策：充血模型

### 决策理由

我们选择**方案2：充血模型**，原因如下：

#### 1. 更符合面向对象原则

```java
// ✅ 好：数据和行为封装在一起
user.isEnabled()

// ❌ 差：数据和行为分离
UserService.isEnabled(user)
```

引用 Martin Fowler 的话：
> "The biggest distinction between procedural and object-oriented programming is that the object-oriented approach combines data with behavior."

#### 2. 符合业界最佳实践

**Spring Security 的设计：**

```java
public interface UserDetails {
    boolean isEnabled();
    boolean isAccountNonExpired();
    boolean isAccountNonLocked();
    boolean isCredentialsNonExpired();
}
```

Spring Security 就是将状态判断方法放在实体中，而不是外部的 Service。

#### 3. 调用更自然，代码更优雅

```java
// ✅ 充血模型：非常直观
UserDO user = userService.getUserByUsername("admin");

if (!user.isEnabled()) {
    throw new BusinessException("账户已被停用");
}

if (!user.isAccountNonExpired()) {
    throw new BusinessException("账户已过期");
}

if (!user.isAccountNonLocked()) {
    throw new BusinessException("账户已被锁定");
}

// 或者更简洁
if (!user.isAccountAvailable()) {
    throw new BusinessException(user.getUnavailableReason());
}
```

```java
// ❌ 贫血模型：需要重复传递 user 对象
if (!UserService.isEnabled(user)) {
    throw new BusinessException("账户已被停用");
}

if (!UserService.isAccountNonExpired(user)) {
    throw new BusinessException("账户已过期");
}

if (!UserService.isAccountNonLocked(user)) {
    throw new BusinessException("账户已被锁定");
}
```

#### 4. 不影响持久化

MyBatis-Plus 只序列化 `@TableField` 标注的字段：

```java
@Data
public class UserDO {
    @TableField("status")  // ✅ 会被持久化
    private Integer status;
    
    // ❌ 不会被持久化
    public boolean isEnabled() {
        return this.status != null && this.status == 0;
    }
}
```

#### 5. 易于测试

```java
// 充血模型：直接创建对象测试
@Test
void testUserStatus() {
    UserDO user = new UserDO();
    user.setStatus(0);
    
    assertTrue(user.isEnabled());  // ✅ 非常简单
}

// 贫血模型：需要通过 Service 测试
@Test
void testUserStatus() {
    UserDO user = new UserDO();
    user.setStatus(0);
    
    assertTrue(UserService.isEnabled(user));  // ⚠️ 需要引入 Service
}
```

## 📖 实际应用示例

### Spring Security UserDetailsService 集成

```java
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 查询用户
        UserDO user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 2. 加载权限
        var authorities = loadUserAuthorities(user);

        // 3. 构建 UserDetails
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                // ✅ 直接调用 UserDO 的方法
                .accountExpired(!user.isAccountNonExpired())
                .accountLocked(!user.isAccountNonLocked())
                .credentialsExpired(!user.isCredentialsNonExpired())
                .disabled(!user.isEnabled())
                .build();
    }
}
```

### 登录失败处理

```java
@Service
@RequiredArgsConstructor
public class LoginFailureHandler {

    private final UserMapper userMapper;

    public void handleLoginFailure(String username) {
        UserDO user = userMapper.selectByUsername(username);
        if (user == null) return;

        int failureCount = incrementFailureCount(username);

        if (failureCount >= 3) {
            // ✅ 直接调用对象方法
            user.lockAccount(60);  // 锁定60分钟
            userMapper.updateById(user);
            
            log.warn("用户 {} 因密码错误{}次被锁定至 {}", 
                username, failureCount, user.getLockedTime());
        }
    }
}
```

### 登录前验证

```java
@Service
public class AuthService {

    public void validateUserBeforeLogin(UserDO user) {
        // ✅ 直接调用对象方法获取详细原因
        String reason = user.getUnavailableReason();
        
        if (reason != null) {
            throw new BusinessException(reason);
        }
    }
}
```

## 🤔 常见疑问

### Q1: DO 中加方法会影响持久化吗？

**A:** 不会。MyBatis-Plus 只序列化 `@TableField` 标注的字段，方法不会被持久化。

```java
@TableField("status")  // ✅ 会被持久化
private Integer status;

public boolean isEnabled() {  // ❌ 不会被持久化
    return this.status != null && this.status == 0;
}
```

### Q2: DO 不是应该只包含数据吗？

**A:** 这是传统贫血模型的观念。现代的领域驱动设计（DDD）推荐充血模型，将数据和行为封装在一起。

参考：
- Spring Security 的 `UserDetails`
- JPA 的 `@Entity` 实体类
- Hibernate 的领域模型

都包含业务方法。

### Q3: 这样会不会让 DO 变得臃肿？

**A:** 不会。我们只在 DO 中添加：
- ✅ **与自身数据紧密相关的判断方法**（如 `isEnabled()`）
- ✅ **简单的状态管理方法**（如 `lockAccount()`）

❌ **不应该在 DO 中添加：**
- 复杂的业务逻辑
- 依赖外部服务的操作
- 需要访问数据库的操作

### Q4: 如果需要访问数据库怎么办？

**A:** 如果判断需要查询数据库，应该放在 Service 层：

```java
// ✅ 简单的状态判断 → 放在 DO 中
public boolean isEnabled() {
    return this.status != null && this.status == 0;
}

// ❌ 需要查询数据库 → 放在 Service 中
public boolean hasPermission(String permission) {
    // 需要查询权限表，应该在 Service 中实现
}
```

## 📚 参考资料

### 贫血模型 vs 充血模型

- [Martin Fowler - Anemic Domain Model](https://martinfowler.com/bliki/AnemicDomainModel.html)
- [DDD - Domain Driven Design](https://domainlanguage.com/ddd/)

### Spring Security 设计

- [UserDetails Interface](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/core/userdetails/UserDetails.html)

### 最佳实践

```java
// ✅ 好的设计：封装数据和行为
public class User {
    private String status;
    
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
}

// ❌ 差的设计：数据和行为分离
public class User {
    private String status;
}

public class UserService {
    public static boolean isActive(User user) {
        return "ACTIVE".equals(user.getStatus());
    }
}
```

## 🎯 总结

| 维度 | 贫血模型 | 充血模型（推荐） |
|------|---------|----------------|
| **调用方式** | `UserService.isEnabled(user)` | `user.isEnabled()` ✅ |
| **封装性** | 差（数据行为分离） | 好（数据行为一起） ✅ |
| **易用性** | 需要传递对象 | 直接调用 ✅ |
| **可测试性** | 需要引入 Service | 直接测试对象 ✅ |
| **符合 OOP** | ❌ | ✅ |
| **业界实践** | 传统三层架构 | Spring Security, JPA ✅ |

**结论：充血模型更符合现代面向对象设计原则，推荐使用。**

---

**版本：** 1.0  
**日期：** 2025-01-04  
**维护者：** Nexus Framework Team

