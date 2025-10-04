# 用户账户状态设计方案

## 📋 设计思路

采用**时间字段判断状态**的方式，而不是使用 boolean 标识字段。这种设计更灵活，更符合实际业务场景。

### 核心原则

1. **DO 只包含数据字段**：不做业务判断逻辑
2. **Service 提供静态辅助方法**：统一的状态判断逻辑
3. **时间字段判断状态**：更灵活，支持自动解锁等场景

## 🗄️ 数据库字段设计

### 字段说明

| 字段名 | 类型 | 说明 | 判断规则 |
|--------|------|------|---------|
| `status` | tinyint | 账户启用状态 | 0=正常，1=停用 |
| `expired_time` | datetime | 账户过期时间 | NULL=永不过期；当前时间>此值=已过期 |
| `locked_time` | datetime | 账户锁定截止时间 | NULL=未锁定；当前时间<此值=被锁定 |
| `password_expire_time` | datetime | 密码过期时间 | NULL=永不过期；当前时间>此值=已过期 |
| `password_update_time` | datetime | 密码最后修改时间 | 用于审计和计算密码有效期 |

### 业务场景

#### 1. 账户启用/停用 - `status`

```sql
-- 正常账户
status = 0

-- 管理员手动停用
status = 1
```

**使用场景：**
- 管理员手动禁用某个用户
- 用户注销账户
- 违规用户封禁

#### 2. 账户过期 - `expired_time`

```sql
-- 永久账户
expired_time = NULL

-- 临时账户（如试用账户、临时工账户）
expired_time = '2025-12-31 23:59:59'
```

**使用场景：**
- 试用账户（30天试用期）
- 临时员工账户
- 项目外包人员账户
- VIP 会员到期

**判断逻辑：**
```java
// 当前时间 > expired_time => 账户已过期
LocalDateTime.now().isAfter(expiredTime)
```

#### 3. 账户锁定 - `locked_time`

```sql
-- 未锁定
locked_time = NULL

-- 锁定1小时（密码错误3次）
locked_time = NOW() + INTERVAL 1 HOUR
-- 例如：locked_time = '2025-01-04 15:30:00'
-- 到了 15:30 后自动解锁
```

**使用场景：**
- 密码连续输错 N 次后锁定
- 异常登录行为检测后临时锁定
- 安全策略触发的临时限制

**判断逻辑：**
```java
// 当前时间 < locked_time => 账户被锁定
// 当前时间 >= locked_time => 锁定已解除（自动解锁）
LocalDateTime.now().isBefore(lockedTime)
```

**优势：**
- ✅ 自动解锁：时间到了自动解除，无需定时任务
- ✅ 灵活性：可以设置不同的锁定时长
- ✅ 可追溯：可以查询锁定历史

#### 4. 密码过期 - `password_expire_time`

```sql
-- 密码永不过期
password_expire_time = NULL

-- 密码90天有效期
password_expire_time = NOW() + INTERVAL 90 DAY
```

**使用场景：**
- 企业安全策略：密码每 90 天必须修改
- 敏感系统：密码每 30 天必须修改
- 临时密码：首次登录必须修改

**判断逻辑：**
```java
// 当前时间 > password_expire_time => 密码已过期
LocalDateTime.now().isAfter(passwordExpireTime)
```

## 💻 代码实现

### 1. 数据库迁移脚本

```sql
-- 文件：V20250104_1__add_user_account_fields.sql

-- 1. 账户过期时间
ALTER TABLE `system_user` ADD COLUMN `expired_time` datetime DEFAULT NULL 
  COMMENT '账户过期时间（NULL=永不过期，当前时间>此值=账户已过期）' AFTER `status`;

-- 2. 账户锁定时间
ALTER TABLE `system_user` ADD COLUMN `locked_time` datetime DEFAULT NULL 
  COMMENT '账户锁定截止时间（NULL=未锁定，当前时间<此值=账户被锁定）' AFTER `expired_time`;

-- 3. 密码过期时间
ALTER TABLE `system_user` ADD COLUMN `password_expire_time` datetime DEFAULT NULL 
  COMMENT '密码过期时间（NULL=永不过期，当前时间>此值=密码已过期）' AFTER `locked_time`;

-- 4. 密码修改时间
ALTER TABLE `system_user` ADD COLUMN `password_update_time` datetime DEFAULT NULL 
  COMMENT '密码最后修改时间' AFTER `password_expire_time`;
```

### 2. UserDO 实体类

```java
@TableName("system_user")
@Data
public class UserDO extends BaseDO {
    
    // ... 其他字段
    
    /**
     * 帐号状态（0=正常 1=停用）
     */
    @TableField("status")
    private Integer status;

    /**
     * 账户过期时间（NULL=永不过期，当前时间>此值=账户已过期）
     */
    @TableField("expired_time")
    private LocalDateTime expiredTime;

    /**
     * 账户锁定截止时间（NULL=未锁定，当前时间<此值=账户被锁定）
     */
    @TableField("locked_time")
    private LocalDateTime lockedTime;

    /**
     * 密码过期时间（NULL=永不过期，当前时间>此值=密码已过期）
     */
    @TableField("password_expire_time")
    private LocalDateTime passwordExpireTime;

    /**
     * 密码最后修改时间
     */
    @TableField("password_update_time")
    private LocalDateTime passwordUpdateTime;
}
```

### 3. UserService 静态辅助方法

```java
public interface UserService {

    // ==================== 状态判断方法 ====================

    /**
     * 判断用户是否已启用
     */
    static boolean isEnabled(UserDO user) {
        if (user == null) return false;
        return user.getStatus() != null && user.getStatus() == 0;
    }

    /**
     * 判断账户是否未过期
     */
    static boolean isAccountNonExpired(UserDO user) {
        if (user == null) return false;
        LocalDateTime expiredTime = user.getExpiredTime();
        if (expiredTime == null) return true; // NULL = 永不过期
        return LocalDateTime.now().isBefore(expiredTime) || LocalDateTime.now().isEqual(expiredTime);
    }

    /**
     * 判断账户是否未锁定
     */
    static boolean isAccountNonLocked(UserDO user) {
        if (user == null) return false;
        LocalDateTime lockedTime = user.getLockedTime();
        if (lockedTime == null) return true; // NULL = 未锁定
        // 当前时间 >= 锁定截止时间 => 锁定已解除
        return LocalDateTime.now().isAfter(lockedTime) || LocalDateTime.now().isEqual(lockedTime);
    }

    /**
     * 判断密码是否未过期
     */
    static boolean isCredentialsNonExpired(UserDO user) {
        if (user == null) return false;
        LocalDateTime passwordExpireTime = user.getPasswordExpireTime();
        if (passwordExpireTime == null) return true; // NULL = 永不过期
        return LocalDateTime.now().isBefore(passwordExpireTime) || LocalDateTime.now().isEqual(passwordExpireTime);
    }

    /**
     * 综合判断账户是否可用
     */
    static boolean isAccountAvailable(UserDO user) {
        return isEnabled(user) 
            && isAccountNonExpired(user) 
            && isAccountNonLocked(user) 
            && isCredentialsNonExpired(user);
    }

    /**
     * 获取账户不可用的原因
     */
    static String getUnavailableReason(UserDO user) {
        if (user == null) return "用户不存在";
        if (!isEnabled(user)) return "账户已被停用";
        if (!isAccountNonExpired(user)) return "账户已过期";
        if (!isAccountNonLocked(user)) return "账户已被锁定";
        if (!isCredentialsNonExpired(user)) return "密码已过期，请修改密码";
        return null;
    }

    // ==================== 状态管理方法 ====================

    /**
     * 锁定账户
     * @param durationMinutes 锁定时长（分钟）
     */
    static LocalDateTime lockAccount(UserDO user, int durationMinutes) {
        if (user == null) return null;
        LocalDateTime lockedTime = LocalDateTime.now().plusMinutes(durationMinutes);
        user.setLockedTime(lockedTime);
        return lockedTime;
    }

    /**
     * 解锁账户
     */
    static void unlockAccount(UserDO user) {
        if (user != null) {
            user.setLockedTime(null);
        }
    }

    /**
     * 设置密码过期时间
     * @param durationDays 有效天数
     */
    static LocalDateTime setPasswordExpireTime(UserDO user, int durationDays) {
        if (user == null) return null;
        LocalDateTime expireTime = LocalDateTime.now().plusDays(durationDays);
        user.setPasswordExpireTime(expireTime);
        user.setPasswordUpdateTime(LocalDateTime.now());
        return expireTime;
    }
}
```

## 🔧 使用示例

### 1. Spring Security UserDetailsService 集成

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

        // 2. 加载权限（这里简化处理）
        var authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        // 3. 构建 UserDetails（使用静态辅助方法判断状态）
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                // ✅ 使用 UserService 的静态方法判断状态
                .accountExpired(!UserService.isAccountNonExpired(user))
                .accountLocked(!UserService.isAccountNonLocked(user))
                .credentialsExpired(!UserService.isCredentialsNonExpired(user))
                .disabled(!UserService.isEnabled(user))
                .build();
    }
}
```

### 2. 登录失败处理 - 连续密码错误锁定

```java
@Service
@RequiredArgsConstructor
public class LoginFailureHandler {

    private final UserMapper userMapper;
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final int LOCK_DURATION_MINUTES = 60; // 锁定1小时

    public void handleLoginFailure(String username) {
        UserDO user = userMapper.selectByUsername(username);
        if (user == null) return;

        // 增加失败次数（这里需要额外的字段记录失败次数）
        int failureCount = getFailureCount(username);
        failureCount++;

        if (failureCount >= MAX_LOGIN_ATTEMPTS) {
            // ✅ 锁定账户1小时
            UserService.lockAccount(user, LOCK_DURATION_MINUTES);
            userMapper.updateById(user);
            
            log.warn("用户 {} 因密码错误{}次被锁定至 {}", 
                username, failureCount, user.getLockedTime());
        }

        saveFailureCount(username, failureCount);
    }

    public void handleLoginSuccess(String username) {
        // 登录成功，清除失败次数
        clearFailureCount(username);
    }
}
```

### 3. 密码修改 - 自动设置密码过期时间

```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private static final int PASSWORD_VALIDITY_DAYS = 90; // 密码90天有效

    public void updatePassword(Long userId, String newPassword) {
        UserDO user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        
        // ✅ 设置密码90天后过期
        UserService.setPasswordExpireTime(user, PASSWORD_VALIDITY_DAYS);
        
        userMapper.updateById(user);
    }
}
```

### 4. 创建临时账户 - 设置账户有效期

```java
@Service
public class TemporaryAccountService {

    public UserDO createTrialAccount(String username) {
        UserDO user = new UserDO();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("trial123"));
        user.setStatus(0); // 启用
        
        // ✅ 设置30天试用期
        user.setExpiredTime(LocalDateTime.now().plusDays(30));
        
        // ✅ 首次登录必须修改密码（密码立即过期）
        user.setPasswordExpireTime(LocalDateTime.now());
        
        userMapper.insert(user);
        return user;
    }
}
```

### 5. 登录前检查 - 友好的错误提示

```java
@Service
public class AuthService {

    public void validateUserBeforeLogin(UserDO user) {
        // ✅ 使用 getUnavailableReason 获取详细原因
        String reason = UserService.getUnavailableReason(user);
        
        if (reason != null) {
            throw new BusinessException(reason);
        }
    }
}
```

### 6. 定时任务 - 清理过期账户

```java
@Component
public class UserCleanupTask {

    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void cleanupExpiredAccounts() {
        // 查询已过期的账户
        List<UserDO> expiredUsers = userMapper.selectList(
            new LambdaQueryWrapper<UserDO>()
                .isNotNull(UserDO::getExpiredTime)
                .le(UserDO::getExpiredTime, LocalDateTime.now())
        );

        for (UserDO user : expiredUsers) {
            // 停用或删除过期账户
            log.info("账户 {} 已过期，执行清理操作", user.getUsername());
            // user.setStatus(1); // 停用
            // 或者删除
        }
    }
}
```

## 📊 状态判断流程图

```
用户登录请求
    ↓
加载用户信息（UserDO）
    ↓
┌─────────────────────────────────────┐
│ UserService 静态方法判断状态         │
├─────────────────────────────────────┤
│ 1. isEnabled(user)                  │ → status == 0?
│    ↓ No → "账户已被停用"            │
│                                      │
│ 2. isAccountNonExpired(user)        │ → now <= expired_time?
│    ↓ No → "账户已过期"              │
│                                      │
│ 3. isAccountNonLocked(user)         │ → now >= locked_time?
│    ↓ No → "账户已被锁定"            │
│                                      │
│ 4. isCredentialsNonExpired(user)    │ → now <= password_expire_time?
│    ↓ No → "密码已过期"              │
│                                      │
│ ✅ All Pass → 账户可用               │
└─────────────────────────────────────┘
    ↓
构建 Spring Security UserDetails
    ↓
继续认证流程
```

## 🎯 设计优势

### 1. 灵活性

| 传统方案（boolean） | 本方案（时间字段） |
|-------------------|------------------|
| 锁定后需要手动解锁 | 时间到了自动解锁 |
| 无法设置锁定时长 | 可以灵活设置（1小时、1天等） |
| 无法记录锁定历史 | 可以查询锁定时间点 |
| 扩展性差 | 易于扩展新的时间策略 |

### 2. 符合业务场景

```java
// ❌ 传统方案：无法表达"锁定1小时"
account_locked = true  // 只能表示"被锁定"，何时解锁？

// ✅ 本方案：清晰表达"锁定到15:30"
locked_time = '2025-01-04 15:30:00'  // 15:30自动解锁
```

### 3. 减少定时任务

```java
// ❌ 传统方案：需要定时任务检查并解锁
@Scheduled(fixedRate = 60000) // 每分钟检查一次
public void unlockExpiredAccounts() {
    // 查询并解锁...
}

// ✅ 本方案：判断时自动识别是否已解锁
if (now >= user.getLockedTime()) {
    // 已过锁定时间，自动视为已解锁
}
```

### 4. DO 职责清晰

```java
// ✅ DO 只包含数据字段
@TableField("locked_time")
private LocalDateTime lockedTime;

// ✅ Service 提供判断逻辑
static boolean isAccountNonLocked(UserDO user) {
    // 业务判断逻辑
}
```

## 🔒 安全建议

### 1. 密码错误锁定策略

```java
// 配置建议
MAX_LOGIN_ATTEMPTS = 3        // 最多尝试3次
LOCK_DURATION_MINUTES = 60    // 锁定1小时
LOCK_DURATION_INCREMENTAL = true  // 逐次递增锁定时长

// 首次：锁定1小时
// 第二次：锁定2小时
// 第三次：锁定4小时
// 第四次：永久锁定（需人工解锁）
```

### 2. 密码有效期策略

```java
// 不同安全等级的建议
普通系统：90天
企业系统：60天
敏感系统：30天
金融系统：7-14天
```

### 3. 账户过期清理

```java
// 建议定期清理过期账户
- 过期30天：发送通知
- 过期60天：停用账户
- 过期90天：删除账户数据
```

## 📝 总结

| 特性 | 说明 |
|------|------|
| **设计原则** | 时间字段判断状态，DO只存数据，Service提供静态方法 |
| **核心字段** | status, expired_time, locked_time, password_expire_time |
| **判断逻辑** | 与当前时间比较，NULL表示永久有效 |
| **自动解锁** | ✅ 时间到了自动解除锁定，无需定时任务 |
| **灵活性** | ✅ 可设置不同的锁定/过期时长 |
| **可追溯** | ✅ 可查询锁定/过期的具体时间点 |
| **扩展性** | ✅ 易于添加新的时间策略字段 |

---

**版本：** 1.0  
**日期：** 2025-01-04  
**维护者：** Nexus Framework Team

