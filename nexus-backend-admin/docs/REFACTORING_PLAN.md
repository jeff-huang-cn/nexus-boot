# Service 层规范重构计划

> **请 Review 此计划后批准执行**

---

## 📋 重构目标

按照以下规则重构 Service 层：

1. **page 分页查询** - 参数:VO, 返回:`PageResult<VO>` ✅ 在Service组装
2. **create/update** - 参数:VO, 返回:`Long/void/DO` ✅ 在Service转换
3. **get/getById** - 参数:`Long`, 返回:`DO` ❌ **需要修改**
4. **getList** - 参数:`QueryVO`, 返回:`List<DO>` ❌ **需要修改**
5. **delete** - 参数:`Long/List<Long>`, 返回:`void` ✅ 已符合

---

## 🎯 实施策略

### 阶段1：修复代码生成器模板（最高优先级 ⭐⭐⭐⭐⭐）
**原因**：后续所有生成的代码都会符合规范
**影响**：所有新生成的代码
**工作量**：2-3小时

### 阶段2：修复 MenuService（中优先级 ⭐⭐⭐）
**原因**：MenuService NullPointerException 需要修复
**影响**：菜单功能
**工作量**：1-2小时

### 阶段3：修复 RoleService（中优先级 ⭐⭐⭐）
**原因**：配合 MenuService 一起修复
**影响**：角色功能
**工作量**：1小时

### 阶段4：修复 UserService（低优先级 ⭐⭐）
**原因**：可以后续慢慢重构
**影响**：用户功能
**工作量**：1小时

---

## 📝 详细实施计划

### Phase 1: 修复代码生成器模板（P0）

#### 任务1.1：修改 Service 接口模板
**文件**：`templates/java/service/service.vm`

```velocity
// 修改前（第52行）
${className}RespVO getById(Long id);

// 修改后
${className}DO getById(Long id);
```

```velocity
// 修改前（第68行）
List<${className}RespVO> getList(${className}PageReqVO pageReqVO);

// 修改后
List<${className}DO> getList(${className}PageReqVO pageReqVO);
```

---

#### 任务1.2：修改 Service 实现模板
**文件**：`templates/java/service/serviceImpl.vm`

```velocity
// 修改前（第70-73行）
@Override
public ${className}RespVO getById(Long id) {
    ${className}DO ${classNameFirstLower} = ${classNameFirstLower}Mapper.selectById(id);
    return BeanUtil.copyProperties(${classNameFirstLower}, ${className}RespVO.class);
}

// 修改后
@Override
public ${className}DO getById(Long id) {
    ${className}DO ${classNameFirstLower} = ${classNameFirstLower}Mapper.selectById(id);
    if (${classNameFirstLower} == null) {
        throw new RuntimeException("${classComment}不存在");
    }
    return ${classNameFirstLower};
}
```

```velocity
// 修改前（第93-104行）
@Override
public List<${className}RespVO> getList(${className}PageReqVO pageReqVO) {
    LambdaQueryWrapper<${className}DO> wrapper = buildQueryWrapper(pageReqVO);
    List<${className}DO> list = ${classNameFirstLower}Mapper.selectList(wrapper);
    // 转换为 VO
    return list.stream()
            .map(item -> BeanUtil.copyProperties(item, ${className}RespVO.class))
            .collect(Collectors.toList());
}

// 修改后
@Override
public List<${className}DO> getList(${className}PageReqVO pageReqVO) {
    LambdaQueryWrapper<${className}DO> wrapper = buildQueryWrapper(pageReqVO);
    return ${classNameFirstLower}Mapper.selectList(wrapper);
}
```

---

#### 任务1.3：修改 Controller 模板
**文件**：`templates/java/controller/controller.vm`

```velocity
// 修改前（第70-74行）
@GetMapping("/get/{id}")
public Result<${className}RespVO> getById(@PathVariable("id") Long id) {
    ${className}RespVO respVO = ${classNameFirstLower}Service.getById(id);
    return Result.success(respVO);
}

// 修改后
@GetMapping("/get/{id}")
public Result<${className}RespVO> getById(@PathVariable("id") Long id) {
    ${className}DO ${classNameFirstLower} = ${classNameFirstLower}Service.getById(id);
    ${className}RespVO respVO = BeanUtil.copyProperties(${classNameFirstLower}, ${className}RespVO.class);
    return Result.success(respVO);
}
```

```velocity
// 修改前（第89-92行）
@GetMapping("/export")
public void export(@Valid ${className}PageReqVO pageReqVO) {
    List<${className}RespVO> list = ${classNameFirstLower}Service.getList(pageReqVO);
    // TODO: 实现 Excel 导出逻辑
}

// 修改后
@GetMapping("/export")
public void export(@Valid ${className}PageReqVO pageReqVO) {
    List<${className}DO> list = ${classNameFirstLower}Service.getList(pageReqVO);
    
    // 转换为 VO
    List<${className}RespVO> voList = list.stream()
            .map(item -> BeanUtil.copyProperties(item, ${className}RespVO.class))
            .collect(Collectors.toList());
    
    // TODO: 实现 Excel 导出逻辑
}
```

**需要添加的import**：
```velocity
import ${packageName}.dal.dataobject.${businessName}.${className}DO;
import java.util.stream.Collectors;
```

---

### Phase 2: 修复 MenuService（P1）

#### 任务2.1：修改 MenuService 接口
**文件**：`MenuService.java`

```java
// 修改前
MenuRespVO getById(Long id);
List<MenuRespVO> getMenuTree();

// 修改后
MenuDO getById(Long id);
List<MenuDO> getMenuList();  // 改名：getMenuTree -> getMenuList
```

---

#### 任务2.2：修改 MenuServiceImpl 实现
**文件**：`MenuServiceImpl.java`

```java
// 修改前
@Override
public MenuRespVO getById(Long id) {
    MenuDO menu = menuMapper.selectById(id);
    if (menu == null) {
        throw new RuntimeException("菜单不存在");
    }
    return BeanUtil.copyProperties(menu, MenuRespVO.class);
}

// 修改后
@Override
public MenuDO getById(Long id) {
    MenuDO menu = menuMapper.selectById(id);
    if (menu == null) {
        throw new RuntimeException("菜单不存在");
    }
    return menu;
}
```

```java
// 修改前
@Override
public List<MenuRespVO> getMenuTree() {
    List<MenuDO> menuList = menuMapper.selectList(
            new LambdaQueryWrapper<MenuDO>()
                    .orderByAsc(MenuDO::getSort));
    
    // 转换为 VO
    List<MenuRespVO> menuVOList = menuList.stream()
            .map(menu -> BeanUtil.copyProperties(menu, MenuRespVO.class))
            .collect(Collectors.toList());
    
    // 构建树形结构
    return buildMenuTree(menuVOList, 0L);
}

// 修改后（移除树形结构构建和VO转换）
@Override
public List<MenuDO> getMenuList() {
    return menuMapper.selectList(
            new LambdaQueryWrapper<MenuDO>()
                    .orderByAsc(MenuDO::getSort));
}

// 删除 buildMenuTree 方法（移到 Controller）
```

---

#### 任务2.3：修改 MenuController
**文件**：`MenuController.java`

```java
// 修改前
@GetMapping("/{id}")
public Result<MenuRespVO> getById(@PathVariable Long id) {
    MenuRespVO menu = menuService.getById(id);
    return Result.success(menu);
}

// 修改后（添加 DO->VO 转换）
@GetMapping("/{id}")
public Result<MenuRespVO> getById(@PathVariable Long id) {
    MenuDO menu = menuService.getById(id);
    MenuRespVO vo = BeanUtil.copyProperties(menu, MenuRespVO.class);
    return Result.success(vo);
}
```

```java
// 修改前
@GetMapping("/tree")
public Result<List<MenuRespVO>> getMenuTree() {
    List<MenuRespVO> menuTree = menuService.getMenuTree();
    return Result.success(menuTree);
}

// 修改后（添加 DO->VO 转换和树形结构构建）
@GetMapping("/tree")
public Result<List<MenuRespVO>> getMenuTree() {
    // 1. Service 返回 DO 列表
    List<MenuDO> menuList = menuService.getMenuList();
    
    // 2. Controller 转换为 VO
    List<MenuRespVO> menuVOList = menuList.stream()
            .map(menu -> BeanUtil.copyProperties(menu, MenuRespVO.class))
            .collect(Collectors.toList());
    
    // 3. Controller 构建树形结构
    List<MenuRespVO> menuTree = buildMenuTree(menuVOList, 0L);
    
    return Result.success(menuTree);
}

// 将 buildMenuTree 方法从 Service 移到 Controller
private List<MenuRespVO> buildMenuTree(List<MenuRespVO> menuList, Long parentId) {
    List<MenuRespVO> result = new ArrayList<>();
    
    // 过滤掉 parentId 为 null 的菜单，并按父ID分组
    Map<Long, List<MenuRespVO>> menuMap = menuList.stream()
            .filter(menu -> menu.getParentId() != null)
            .collect(Collectors.groupingBy(MenuRespVO::getParentId));
    
    // 递归构建树
    for (MenuRespVO menu : menuList) {
        Long menuParentId = menu.getParentId() != null ? menu.getParentId() : 0L;
        
        if (menuParentId.equals(parentId)) {
            List<MenuRespVO> children = menuMap.get(menu.getId());
            if (children != null && !children.isEmpty()) {
                menu.setChildren(children);
            }
            result.add(menu);
        }
    }
    
    return result;
}
```

**需要添加的import**：
```java
import com.nexus.backend.admin.dal.dataobject.permission.MenuDO;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Map;
```

---

### Phase 3: 修复 RoleService（P1）

#### 任务3.1：修改 RoleService 接口
**文件**：`RoleService.java`

```java
// 修改前
RoleRespVO getById(Long id);
List<RoleRespVO> getList();

// 修改后
RoleDO getById(Long id);
List<RoleDO> getList();
```

---

#### 任务3.2：修改 RoleServiceImpl 实现
**文件**：`RoleServiceImpl.java`

```java
// 修改前
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

// 修改后
@Override
public RoleDO getById(Long id) {
    RoleDO role = roleMapper.selectById(id);
    if (role == null) {
        throw new RuntimeException("角色不存在");
    }
    return role;
}

@Override
public List<RoleDO> getList() {
    return roleMapper.selectList(new LambdaQueryWrapper<>());
}
```

---

#### 任务3.3：修改 RoleController
**文件**：`RoleController.java`

```java
// 修改前
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

// 修改后
@GetMapping("/{id}")
public Result<RoleRespVO> getById(@PathVariable Long id) {
    RoleDO role = roleService.getById(id);
    RoleRespVO vo = BeanUtil.copyProperties(role, RoleRespVO.class);
    return Result.success(vo);
}

@GetMapping("/list")
public Result<List<RoleRespVO>> getList() {
    List<RoleDO> list = roleService.getList();
    List<RoleRespVO> voList = list.stream()
            .map(role -> BeanUtil.copyProperties(role, RoleRespVO.class))
            .collect(Collectors.toList());
    return Result.success(voList);
}
```

**需要添加的import**：
```java
import com.nexus.backend.admin.dal.dataobject.permission.RoleDO;
import java.util.stream.Collectors;
```

---

### Phase 4: 修复 UserService（P2）

#### 任务4.1：修改 UserService 接口
**文件**：`UserService.java`

```java
// 修改前
UserRespVO getById(Long id);
List<UserRespVO> getList(UserPageReqVO pageReqVO);

// 修改后
UserDO getById(Long id);
List<UserDO> getList(UserPageReqVO pageReqVO);
```

---

#### 任务4.2：修改 UserServiceImpl 实现
**文件**：`UserServiceImpl.java`

```java
// 修改前
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

// 修改后
@Override
public UserDO getById(Long id) {
    UserDO user = userMapper.selectById(id);
    if (user == null) {
        throw new RuntimeException("用户不存在");
    }
    return user;
}

@Override
public List<UserDO> getList(UserPageReqVO pageReqVO) {
    LambdaQueryWrapper<UserDO> wrapper = buildQueryWrapper(pageReqVO);
    return userMapper.selectList(wrapper);
}
```

---

#### 任务4.3：修改 UserController
**文件**：`UserController.java`

```java
// 修改前
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

// 修改后
@GetMapping("/get/{id}")
public Result<UserRespVO> getById(@PathVariable("id") Long id) {
    UserDO user = userService.getById(id);
    UserRespVO vo = BeanUtil.copyProperties(user, UserRespVO.class);
    return Result.success(vo);
}

@GetMapping("/export")
public void export(@Valid UserPageReqVO pageReqVO) {
    List<UserDO> list = userService.getList(pageReqVO);
    
    // 转换为 VO
    List<UserRespVO> voList = list.stream()
            .map(user -> BeanUtil.copyProperties(user, UserRespVO.class))
            .collect(Collectors.toList());
    
    // TODO: 实现 Excel 导出逻辑
}
```

**需要添加的import**：
```java
import com.nexus.backend.admin.dal.dataobject.user.UserDO;
import java.util.stream.Collectors;
```

---

## 📊 实施时间表

| 阶段 | 任务 | 预计工作量 | 依赖 | 验收标准 |
|------|------|-----------|------|---------|
| **Phase 1** | 代码生成器模板 | 2-3小时 | 无 | 生成新代码符合规范 |
| **Phase 2** | MenuService | 1-2小时 | Phase 1 | 功能正常 + NullPointer修复 |
| **Phase 3** | RoleService | 1小时 | Phase 1 | 功能正常 |
| **Phase 4** | UserService | 1小时 | Phase 1 | 功能正常 |
| **总计** | - | **5-7小时** | - | 所有功能正常 |

---

## ✅ 验收标准

### 功能验收
- [ ] 所有修改的功能正常工作
- [ ] 前端调用正常
- [ ] 没有编译错误
- [ ] 没有运行时错误

### 代码质量
- [ ] 符合新的 Service 层规范
- [ ] 没有 DO->VO 转换在 Service 层
- [ ] Controller 负责所有 VO 转换
- [ ] 代码清晰易读

### 测试验收
- [ ] 手动测试所有修改的接口
- [ ] 测试 MenuService 的 NullPointerException 已修复
- [ ] 测试菜单树构建正常
- [ ] 测试分页查询正常

---

## 🚨 风险评估

### 低风险 ⭐
- **Phase 1**：代码生成器模板修改，只影响新生成的代码
- **Phase 3/4**：简单的 Service 重构，逻辑清晰

### 中风险 ⭐⭐
- **Phase 2**：MenuService 涉及树形结构构建逻辑移动

### 降低风险措施
1. 每个 Phase 完成后立即测试
2. 保留原有代码的备份
3. 使用 Git 分支进行修改
4. 修改后立即运行测试

---

## 📝 后续优化建议

### 短期优化（1-2周）
1. 为所有 Service 方法添加单元测试
2. 统一异常处理（不要用 RuntimeException）
3. 添加参数校验

### 长期优化（1-2月）
1. 引入 MapStruct 自动化 DTO/VO 转换
2. 复杂查询考虑引入 DTO（如用户+角色+部门）
3. 性能优化：批量查询避免 N+1

---

## 🎯 总结

### 核心改动
- **getById()** 和 **getList()** 从返回 VO 改为返回 DO
- **Controller 负责 DO->VO 转换**
- **树形结构构建移到 Controller**

### 影响范围
- 代码生成器模板：3个文件
- MenuService：3个文件
- RoleService：3个文件
- UserService：3个文件
- **总计：12个文件，约24处修改**

### 预期收益
- ✅ Service 层职责更清晰
- ✅ 代码复用性更强（Service 间调用返回 DO）
- ✅ 符合统一规范
- ✅ 后续维护更简单

---

**请 Review 此计划，确认后我立即开始执行！** 🚀

