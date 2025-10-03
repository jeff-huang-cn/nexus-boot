# Service 层规则检查清单

## 📋 规则定义

```
┌──────────────────────────────────────────────────────┐
│ Service 方法设计规则                                  │
├──────────────────────────────────────────────────────┤
│ 1. page 分页查询                                      │
│    参数: VO | 返回: PageResult<VO>                   │
│    原因: 主要给前端用，业务紧密，在Service组装好数据    │
│                                                       │
│ 2. create/update                                     │
│    参数: VO | 返回: Long/void/DO                     │
│    原因: VO需要复杂处理(多部门/角色等)                 │
│                                                       │
│ 3. get/getById                                       │
│    参数: Long | 返回: DO                             │
│    原因: 模板生成，后续根据业务扩展                     │
│                                                       │
│ 4. getList                                           │
│    参数: QueryVO | 返回: List<DO>                    │
│    原因: 与get逻辑一致                                 │
│                                                       │
│ 5. delete/deleteBatch                                │
│    参数: Long/List<Long> | 返回: void                │
│    原因: 简单操作，返回成功与否                         │
└──────────────────────────────────────────────────────┘
```

---

## ❌ 检查结果：不符合规则的代码

### 1. 代码生成器模板

#### ❌ `templates/java/service/service.vm`
```velocity
// 第52行 - getById 返回了 VO
${className}RespVO getById(Long id);

// 第68行 - getList 返回了 VO
List<${className}RespVO> getList(${className}PageReqVO pageReqVO);
```

**问题**：
- `getById()` 应该返回 `${className}DO`
- `getList()` 应该返回 `List<${className}DO>`

---

#### ❌ `templates/java/service/serviceImpl.vm`
```velocity
// 第70-73行 - getById 返回了 VO
@Override
public ${className}RespVO getById(Long id) {
    ${className}DO ${classNameFirstLower} = ${classNameFirstLower}Mapper.selectById(id);
    return BeanUtil.copyProperties(${classNameFirstLower}, ${className}RespVO.class);
}

// 第93-104行 - getList 返回了 VO
@Override
public List<${className}RespVO> getList(${className}PageReqVO pageReqVO) {
    LambdaQueryWrapper<${className}DO> wrapper = buildQueryWrapper(pageReqVO);
    List<${className}DO> list = ${classNameFirstLower}Mapper.selectList(wrapper);
    // 转换为 VO
    return list.stream()
            .map(item -> BeanUtil.copyProperties(item, ${className}RespVO.class))
            .collect(Collectors.toList());
}
```

**问题**：
- `getById()` 内部做了 DO->VO 转换
- `getList()` 内部做了 DO->VO 转换

---

#### ❌ `templates/java/controller/controller.vm`
```velocity
// 第70-74行 - Controller 直接返回 Service 的 VO
@GetMapping("/get/{id}")
public Result<${className}RespVO> getById(@PathVariable("id") Long id) {
    ${className}RespVO respVO = ${classNameFirstLower}Service.getById(id);
    return Result.success(respVO);
}

// 第89-92行 - Controller 直接返回 Service 的 VO
@GetMapping("/export")
public void export(@Valid ${className}PageReqVO pageReqVO) {
    List<${className}RespVO> list = ${classNameFirstLower}Service.getList(pageReqVO);
    // TODO: 实现 Excel 导出逻辑
}
```

**问题**：
- Controller 应该负责 DO->VO 转换，而不是直接使用 Service 返回的 VO

---

### 2. MenuService

#### ❌ `MenuService.java`
```java
// 第43行 - getById 返回了 VO
MenuRespVO getById(Long id);

// 第50行 - getMenuTree 返回了 VO（类似 getList）
List<MenuRespVO> getMenuTree();
```

**问题**：
- `getById()` 应该返回 `MenuDO`
- `getMenuTree()` 应该返回 `List<MenuDO>`

---

#### ❌ `MenuServiceImpl.java`
```java
// 第84-90行
@Override
public MenuRespVO getById(Long id) {
    MenuDO menu = menuMapper.selectById(id);
    if (menu == null) {
        throw new RuntimeException("菜单不存在");
    }
    return BeanUtil.copyProperties(menu, MenuRespVO.class);
}

// 第93-106行
@Override
public List<MenuRespVO> getMenuTree() {
    List<MenuDO> menuList = menuMapper.selectList(...);
    // 转换为 VO
    List<MenuRespVO> menuVOList = menuList.stream()
            .map(menu -> BeanUtil.copyProperties(menu, MenuRespVO.class))
            .collect(Collectors.toList());
    // 构建树形结构
    return buildMenuTree(menuVOList, 0L);
}
```

**问题**：
- Service 内部做了 DO->VO 转换
- 树形结构构建应该在 Controller 层

---

#### ❌ `MenuController.java`
```java
// 第57-60行
@GetMapping("/{id}")
public Result<MenuRespVO> getById(@PathVariable Long id) {
    MenuRespVO menu = menuService.getById(id);
    return Result.success(menu);
}

// 第65-69行
@GetMapping("/tree")
public Result<List<MenuRespVO>> getMenuTree() {
    List<MenuRespVO> menuTree = menuService.getMenuTree();
    return Result.success(menuTree);
}
```

**问题**：
- Controller 应该负责 DO->VO 转换和树形结构构建

---

### 3. RoleService

#### ❌ `RoleService.java`
```java
// 第44行 - getById 返回了 VO
RoleRespVO getById(Long id);

// 第51行 - getList 返回了 VO
List<RoleRespVO> getList();
```

**问题**：
- `getById()` 应该返回 `RoleDO`
- `getList()` 应该返回 `List<RoleDO>`

---

#### ❌ `RoleServiceImpl.java`
```java
@Override
public RoleRespVO getById(Long id) {
    RoleDO role = roleMapper.selectById(id);
    if (role == null) {
        throw new RuntimeException("角色不存在");
    }
    return BeanUtil.copyProperties(role, RoleRespVO.class);
}

@Override
public List<RoleRespVO> getList() {
    List<RoleDO> roleList = roleMapper.selectList(new LambdaQueryWrapper<>());
    return roleList.stream()
            .map(role -> BeanUtil.copyProperties(role, RoleRespVO.class))
            .collect(Collectors.toList());
}
```

**问题**：
- Service 内部做了 DO->VO 转换

---

#### ❌ `RoleController.java`
```java
@GetMapping("/{id}")
public Result<RoleRespVO> getById(@PathVariable Long id) {
    RoleRespVO role = roleService.getById(id);
    return Result.success(role);
}

@GetMapping("/list")
public Result<List<RoleRespVO>> getList() {
    List<RoleRespVO> list = roleService.getList();
    return Result.success(list);
}
```

**问题**：
- Controller 应该负责 DO->VO 转换

---

### 4. UserService

#### ❌ `UserService.java`
```java
// 第54行 - getById 返回了 VO
UserRespVO getById(Long id);

// 第70行 - getList 返回了 VO
List<UserRespVO> getList(UserPageReqVO pageReqVO);
```

**问题**：
- `getById()` 应该返回 `UserDO`
- `getList()` 应该返回 `List<UserDO>`

---

#### ❌ `UserServiceImpl.java`
```java
@Override
public UserRespVO getById(Long id) {
    UserDO user = userMapper.selectById(id);
    return BeanUtil.copyProperties(user, UserRespVO.class);
}

@Override
public List<UserRespVO> getList(UserPageReqVO pageReqVO) {
    LambdaQueryWrapper<UserDO> wrapper = buildQueryWrapper(pageReqVO);
    List<UserDO> list = userMapper.selectList(wrapper);
    return list.stream()
            .map(item -> BeanUtil.copyProperties(item, UserRespVO.class))
            .collect(Collectors.toList());
}
```

**问题**：
- Service 内部做了 DO->VO 转换

---

#### ❌ `UserController.java`
```java
@GetMapping("/get/{id}")
public Result<UserRespVO> getById(@PathVariable("id") Long id) {
    UserRespVO respVO = userService.getById(id);
    return Result.success(respVO);
}

@GetMapping("/export")
public void export(@Valid UserPageReqVO pageReqVO) {
    List<UserRespVO> list = userService.getList(pageReqVO);
    // TODO: 实现 Excel 导出逻辑
}
```

**问题**：
- Controller 应该负责 DO->VO 转换

---

## ✅ 符合规则的代码

### 1. 代码生成器模板

#### ✅ `templates/java/service/service.vm`
```velocity
// 第23行 - create 参数接收 VO，返回 Long
Long create(@Valid ${className}SaveReqVO createReqVO);

// 第30行 - update 参数接收 VO，返回 void
void update(@Valid ${className}SaveReqVO updateReqVO);

// 第37行 - delete 返回 void
void delete(Long id);

// 第44行 - deleteBatch 返回 void
void deleteBatch(List<Long> ids);

// 第60行 - getPage 返回 PageResult<VO>
PageResult<${className}RespVO> getPage(${className}PageReqVO pageReqVO);
```

#### ✅ `templates/java/service/serviceImpl.vm`
```velocity
// create - 参数VO，在Service内转DO，返回Long
@Override
public Long create(${className}SaveReqVO createReqVO) {
    ${className}DO ${classNameFirstLower} = BeanUtil.copyProperties(createReqVO, ${className}DO.class);
    ${classNameFirstLower}Mapper.insert(${classNameFirstLower});
    return ${classNameFirstLower}.getId();
}

// update - 参数VO，在Service内转DO
@Override
public void update(${className}SaveReqVO updateReqVO) {
    validateExists(updateReqVO.getId());
    ${className}DO update${className} = BeanUtil.copyProperties(updateReqVO, ${className}DO.class);
    ${classNameFirstLower}Mapper.updateById(update${className});
}

// getPage - 返回 PageResult<VO>，在Service内组装
@Override
public PageResult<${className}RespVO> getPage(${className}PageReqVO pageReqVO) {
    LambdaQueryWrapper<${className}DO> wrapper = buildQueryWrapper(pageReqVO);
    IPage<${className}DO> page = new Page<>(pageReqVO.getPageNum(), pageReqVO.getPageSize());
    IPage<${className}DO> result = ${classNameFirstLower}Mapper.selectPage(page, wrapper);
    
    List<${className}RespVO> list = result.getRecords().stream()
            .map(item -> BeanUtil.copyProperties(item, ${className}RespVO.class))
            .collect(Collectors.toList());
    
    return new PageResult<>(list, result.getTotal());
}
```

---

## 📊 统计

| 模块 | 总方法数 | 不符合规则 | 符合规则 | 合规率 |
|------|---------|-----------|---------|--------|
| **代码生成器模板** | 7 | 2 | 5 | 71% |
| **MenuService** | 6 | 2 | 4 | 67% |
| **RoleService** | 6 | 2 | 4 | 67% |
| **UserService** | 7 | 2 | 5 | 71% |
| **总计** | 26 | 8 | 18 | **69%** |

---

## 📝 修改清单

### 优先级 P0（代码生成器）
- [ ] 修改 `service.vm` - getById 返回 DO
- [ ] 修改 `service.vm` - getList 返回 List<DO>
- [ ] 修改 `serviceImpl.vm` - getById 实现
- [ ] 修改 `serviceImpl.vm` - getList 实现
- [ ] 修改 `controller.vm` - getById 添加 DO->VO 转换
- [ ] 修改 `controller.vm` - export 添加 DO->VO 转换

### 优先级 P1（MenuService）
- [ ] 修改 `MenuService.java` - getById 返回 MenuDO
- [ ] 修改 `MenuService.java` - getMenuTree 返回 List<MenuDO>
- [ ] 修改 `MenuServiceImpl.java` - getById 实现
- [ ] 修改 `MenuServiceImpl.java` - getMenuTree 实现（移除树形结构构建）
- [ ] 修改 `MenuController.java` - getById 添加 DO->VO 转换
- [ ] 修改 `MenuController.java` - getMenuTree 添加 DO->VO 转换和树形结构构建

### 优先级 P1（RoleService）
- [ ] 修改 `RoleService.java` - getById 返回 RoleDO
- [ ] 修改 `RoleService.java` - getList 返回 List<RoleDO>
- [ ] 修改 `RoleServiceImpl.java` - getById 实现
- [ ] 修改 `RoleServiceImpl.java` - getList 实现
- [ ] 修改 `RoleController.java` - getById 添加 DO->VO 转换
- [ ] 修改 `RoleController.java` - getList 添加 DO->VO 转换

### 优先级 P1（UserService）
- [ ] 修改 `UserService.java` - getById 返回 UserDO
- [ ] 修改 `UserService.java` - getList 返回 List<UserDO>
- [ ] 修改 `UserServiceImpl.java` - getById 实现
- [ ] 修改 `UserServiceImpl.java` - getList 实现
- [ ] 修改 `UserController.java` - getById 添加 DO->VO 转换
- [ ] 修改 `UserController.java` - export 添加 DO->VO 转换

---

## 🎯 总结

**当前状态**：69% 方法符合规则
**主要问题**：`getById()` 和 `getList()` 方法返回了 VO 而不是 DO
**影响范围**：4个模块，共24处需要修改

