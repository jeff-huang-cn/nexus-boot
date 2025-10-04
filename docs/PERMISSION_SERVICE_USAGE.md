# PermissionService 使用指南

## 📋 简介

`PermissionService` 是一个权限聚合服务，用于查询用户的按钮权限和菜单权限。

### 核心功能

1. **查询用户的按钮权限**：获取用户所有的按钮权限标识
2. **查询用户的所有权限**：包括目录、菜单、按钮的权限
3. **权限判断**：判断用户是否拥有指定权限

## 🔍 查询逻辑

### 数据库表关系

```
system_user (用户表)
     ↓ 1:N
system_user_role (用户角色关联表)
     ↓ N:1
system_role (角色表)
     ↓ 1:N
system_role_menu (角色菜单关联表)
     ↓ N:1
system_menu (菜单表)
```

### 查询步骤

```java
// 1. 通过 userId 查询 user_role 表获取用户的所有角色ID
List<Long> roleIds = getUserRoleIds(userId);

// 2. 通过 roleIds 查询 role_menu 表获取角色分配的菜单ID
List<Long> menuIds = getRoleMenuIds(roleIds);

// 3. 通过 menuIds 查询 menu 表，过滤条件：
//    - type = 3 (按钮)
//    - status = 1 (启用)
//    - permission IS NOT NULL
List<MenuDO> buttons = menuMapper.selectList(...);

// 4. 提取 permission 字段返回 Set<String>
Set<String> permissions = buttons.stream()
    .map(MenuDO::getPermission)
    .collect(Collectors.toSet());
```

## 💻 使用示例

### 1. 获取用户的按钮权限

```java
@Service
@RequiredArgsConstructor
public class UserAuthService {
    
    private final PermissionService permissionService;
    
    public void loadUserPermissions(Long userId) {
        // 获取用户的所有按钮权限
        Set<String> permissions = permissionService.getUserButtonPermissions(userId);
        
        // 结果示例：
        // ["system:user:create", "system:user:update", "system:user:delete"]
        
        System.out.println("用户拥有的按钮权限: " + permissions);
    }
}
```

### 2. 获取用户的所有权限（包括菜单）

```java
public void loadAllPermissions(Long userId) {
    // 获取所有权限（目录、菜单、按钮）
    Set<String> allPermissions = permissionService.getUserAllPermissions(userId);
    
    // 结果示例：
    // ["system:user", "system:user:query", "system:user:create", ...]
    
    System.out.println("用户拥有的所有权限: " + allPermissions);
}
```

### 3. 判断用户是否拥有权限

```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final PermissionService permissionService;
    
    public void deleteUser(Long userId, Long targetUserId) {
        // 判断是否有删除权限
        if (!permissionService.hasPermission(userId, "system:user:delete")) {
            throw new BusinessException("您没有删除用户的权限");
        }
        
        // 执行删除操作
        // ...
    }
}
```

### 4. 判断用户是否拥有任意一个权限

```java
public void updateUserOrRole(Long userId) {
    // 判断是否拥有用户更新或角色更新权限
    if (!permissionService.hasAnyPermission(userId, 
            "system:user:update", 
            "system:role:update")) {
        throw new BusinessException("您没有更新权限");
    }
    
    // 执行更新操作
    // ...
}
```

### 5. 判断用户是否拥有所有权限

```java
public void importUsers(Long userId) {
    // 批量导入需要同时拥有创建和更新权限
    if (!permissionService.hasAllPermissions(userId, 
            "system:user:create", 
            "system:user:update")) {
        throw new BusinessException("批量导入需要创建和更新权限");
    }
    
    // 执行导入操作
    // ...
}
```

## 🔐 与 Spring Security 集成

### 1. 在 UserDetailsService 中使用

```java
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;
    private final PermissionService permissionService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 查询用户
        UserDO user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 2. 获取用户的所有权限
        Set<String> permissions = permissionService.getUserAllPermissions(user.getId());
        
        // 3. 转换为 GrantedAuthority
        var authorities = permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // 4. 构建 UserDetails
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)  // ✅ 权限列表
                .accountExpired(!user.isAccountNonExpired())
                .accountLocked(!user.isAccountNonLocked())
                .credentialsExpired(!user.isCredentialsNonExpired())
                .disabled(!user.isEnabled())
                .build();
    }
}
```

### 2. 使用 @PreAuthorize 注解

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    /**
     * 创建用户
     * 需要 system:user:create 权限
     */
    @PostMapping
    @PreAuthorize("hasAuthority('system:user:create')")
    public Result<Long> create(@RequestBody UserCreateReq req) {
        // ...
    }

    /**
     * 删除用户
     * 需要 system:user:delete 权限
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        // ...
    }

    /**
     * 批量删除用户
     * 需要 system:user:delete 和 system:user:query 权限
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('system:user:delete') and hasAuthority('system:user:query')")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        // ...
    }

    /**
     * 导出用户
     * 需要 system:user:export 或 system:user:query 权限之一
     */
    @GetMapping("/export")
    @PreAuthorize("hasAnyAuthority('system:user:export', 'system:user:query')")
    public void export(HttpServletResponse response) {
        // ...
    }
}
```

### 3. 自定义权限表达式

```java
@Component("perm")
public class PermissionExpressionRoot {

    @Resource
    private PermissionService permissionService;

    /**
     * 判断当前用户是否拥有指定权限
     */
    public boolean has(String permission) {
        Long userId = SecurityContextUtils.getLoginUserId();
        if (userId == null) {
            return false;
        }
        return permissionService.hasPermission(userId, permission);
    }

    /**
     * 判断当前用户是否拥有任意一个权限
     */
    public boolean hasAny(String... permissions) {
        Long userId = SecurityContextUtils.getLoginUserId();
        if (userId == null) {
            return false;
        }
        return permissionService.hasAnyPermission(userId, permissions);
    }

    /**
     * 判断当前用户是否拥有所有权限
     */
    public boolean hasAll(String... permissions) {
        Long userId = SecurityContextUtils.getLoginUserId();
        if (userId == null) {
            return false;
        }
        return permissionService.hasAllPermissions(userId, permissions);
    }
}

// 使用示例
@RestController
public class UserController {

    @PostMapping("/users")
    @PreAuthorize("@perm.has('system:user:create')")
    public Result<Long> create(@RequestBody UserCreateReq req) {
        // ...
    }

    @DeleteMapping("/users/batch")
    @PreAuthorize("@perm.hasAll('system:user:delete', 'system:user:query')")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        // ...
    }
}
```

## 🎯 最佳实践

### 1. 权限标识命名规范

```
格式：模块:功能:操作

示例：
- system:user:query    // 用户查询
- system:user:create   // 用户创建
- system:user:update   // 用户更新
- system:user:delete   // 用户删除
- system:user:export   // 用户导出
- system:user:import   // 用户导入
- system:role:assign   // 角色分配
```

### 2. 缓存权限数据

对于高频查询，建议使用缓存：

```java
@Service
@RequiredArgsConstructor
public class CachedPermissionService {

    private final PermissionService permissionService;
    private final RedisTemplate<String, Set<String>> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "user:permissions:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    public Set<String> getUserButtonPermissions(Long userId) {
        String cacheKey = CACHE_KEY_PREFIX + userId;
        
        // 1. 尝试从缓存获取
        Set<String> cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // 2. 从数据库查询
        Set<String> permissions = permissionService.getUserButtonPermissions(userId);
        
        // 3. 写入缓存
        redisTemplate.opsForValue().set(cacheKey, permissions, CACHE_TTL);
        
        return permissions;
    }

    /**
     * 清除用户权限缓存
     * 在角色分配、权限修改后调用
     */
    public void evictUserPermissions(Long userId) {
        String cacheKey = CACHE_KEY_PREFIX + userId;
        redisTemplate.delete(cacheKey);
    }
}
```

### 3. 权限变更通知

当角色权限变更时，及时清除相关用户的权限缓存：

```java
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final UserRoleMapper userRoleMapper;
    private final CachedPermissionService cachedPermissionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenu(Long roleId, List<Long> menuIds) {
        // 1. 删除原有的角色菜单关联
        roleMenuMapper.delete(
            new LambdaQueryWrapper<RoleMenuDO>()
                .eq(RoleMenuDO::getRoleId, roleId)
        );

        // 2. 插入新的角色菜单关联
        // ...

        // 3. ✅ 清除拥有该角色的所有用户的权限缓存
        List<UserRoleDO> userRoles = userRoleMapper.selectList(
            new LambdaQueryWrapper<UserRoleDO>()
                .eq(UserRoleDO::getRoleId, roleId)
        );

        userRoles.forEach(userRole -> 
            cachedPermissionService.evictUserPermissions(userRole.getUserId())
        );
    }
}
```

## 🔧 性能优化

### 当前实现（分步查询）

```java
// 步骤1：查询用户角色
List<Long> roleIds = getUserRoleIds(userId);

// 步骤2：查询角色菜单
List<Long> menuIds = getRoleMenuIds(roleIds);

// 步骤3：查询菜单权限
List<MenuDO> menus = menuMapper.selectList(...);
```

**特点：**
- 3次数据库查询
- 代码简单清晰
- 适合中小规模系统

### 性能优化方案（联表查询）

如果需要优化性能，可以改为联表查询：

```java
// 将 MenuMapper 改为继承 MPJBaseMapper
@Mapper
public interface MenuMapper extends MPJBaseMapper<MenuDO> {
}

// 使用联表查询（一次查询完成）
@Override
public Set<String> getUserButtonPermissions(Long userId) {
    List<MenuDO> menus = menuMapper.selectJoinList(
        MenuDO.class,
        new MPJLambdaWrapper<MenuDO>()
            .selectAll(MenuDO.class)
            .innerJoin(RoleMenuDO.class, RoleMenuDO::getMenuId, MenuDO::getId)
            .innerJoin(UserRoleDO.class, UserRoleDO::getRoleId, RoleMenuDO::getRoleId)
            .eq(UserRoleDO::getUserId, userId)
            .eq(MenuDO::getType, MENU_TYPE_BUTTON)
            .eq(MenuDO::getStatus, MENU_STATUS_ENABLE)
            .isNotNull(MenuDO::getPermission)
    );

    return menus.stream()
        .map(MenuDO::getPermission)
        .collect(Collectors.toSet());
}
```

**特点：**
- 1次数据库查询
- 性能更好
- 需要理解联表查询

## 📝 总结

| 方法 | 用途 | 返回值 |
|------|------|--------|
| `getUserButtonPermissions(userId)` | 获取用户按钮权限 | `Set<String>` |
| `getUserAllPermissions(userId)` | 获取用户所有权限 | `Set<String>` |
| `hasPermission(userId, permission)` | 判断是否拥有权限 | `boolean` |
| `hasAnyPermission(userId, permissions)` | 判断是否拥有任意权限 | `boolean` |
| `hasAllPermissions(userId, permissions)` | 判断是否拥有所有权限 | `boolean` |

**核心要点：**
1. ✅ 查询逻辑：用户 → 角色 → 菜单 → 权限
2. ✅ 过滤条件：type=3（按钮）、status=1（启用）
3. ✅ 返回值：权限标识的 `Set<String>`
4. ✅ 性能优化：使用缓存减少数据库查询

---

**版本：** 1.0  
**日期：** 2025-01-04  
**维护者：** Nexus Framework Team

