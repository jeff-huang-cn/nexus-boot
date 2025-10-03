# 树形结构工具使用说明

## 📋 概述

`TreeUtils.buildTree()` 是一个通用的静态工具方法，用于将扁平列表转换为树形结构。

**位置**：`com.nexus.framework.utils.tree.TreeUtils` (framework 模块)

---

## 🔧 方法签名

```java
public static <T> List<T> buildTree(
    List<T> list,                                    // 扁平列表
    Function<T, Long> getId,                         // 获取节点ID的函数
    Function<T, Long> getParentId,                   // 获取父节点ID的函数
    BiConsumer<T, List<T>> setChildren,              // 设置子节点的函数
    Long rootParentId                                // 根节点的父ID（通常为0）
)
```

### 参数说明

| 参数 | 类型 | 说明 |
|------|------|------|
| `list` | `List<T>` | 需要转换的扁平列表 |
| `getId` | `Function<T, Long>` | 获取节点ID的函数（如：`Menu::getId`） |
| `getParentId` | `Function<T, Long>` | 获取父节点ID的函数（如：`Menu::getParentId`） |
| `setChildren` | `BiConsumer<T, List<T>>` | 设置子节点的函数（如：`Menu::setChildren`） |
| `rootParentId` | `Long` | 根节点的父ID，通常为 `0L` |

### 返回值

`List<T>` - 树形结构的根节点列表

---

## 📖 使用示例

### 示例1：在 Controller 中使用（MenuController）

```java
@GetMapping("/tree")
public Result<List<MenuRespVO>> getMenuTree() {
    // 1. Service 返回 DO 列表
    List<MenuDO> menuList = menuService.getMenuList();
    
    // 2. Controller 转换为 VO
    List<MenuRespVO> menuVOList = menuList.stream()
            .map(menu -> BeanUtil.copyProperties(menu, MenuRespVO.class))
            .collect(Collectors.toList());
    
        // 3. 使用 TreeUtils 构建树形结构
        List<MenuRespVO> menuTree = TreeUtils.buildTree(
            menuVOList,
            MenuRespVO::getId,
            MenuRespVO::getParentId,
            MenuRespVO::setChildren,
            0L
    );
    
    return Result.success(menuTree);
}
```

---

### 示例2：在其他 Service 中使用（DeptService）

假设有一个部门服务，也需要构建树形结构：

```java
@Service
public class DeptServiceImpl implements DeptService {
    
    @Resource
    private DeptMapper deptMapper;
    
    @Override
    public List<DeptRespVO> getDeptTree() {
        // 1. 查询所有部门
        List<DeptDO> deptList = deptMapper.selectList(
                new LambdaQueryWrapper<DeptDO>()
                        .orderByAsc(DeptDO::getSort));
        
        // 2. 转换为 VO
        List<DeptRespVO> deptVOList = deptList.stream()
                .map(dept -> BeanUtil.copyProperties(dept, DeptRespVO.class))
                .collect(Collectors.toList());
        
        // 3. 使用 TreeUtils 构建树形结构
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

---

### 示例3：在业务代码中直接使用

```java
public class SomeBusinessService {
    
    public void processData() {
        // 获取扁平的分类列表
        List<CategoryVO> categories = getCategoryList();
        
        // 构建树形结构
        List<CategoryVO> categoryTree = TreeUtils.buildTree(
                categories,
                CategoryVO::getId,
                CategoryVO::getParentId,
                CategoryVO::setChildren,
                0L  // 根节点的父ID为0
        );
        
        // 使用树形结构进行后续处理
        processCategoryTree(categoryTree);
    }
}
```

---

### 示例4：自定义根节点父ID

如果你的数据中根节点的 `parentId` 是 `-1` 或其他值：

```java
// 根节点的 parentId 为 -1
List<MenuRespVO> menuTree = TreeUtils.buildTree(
        menuVOList,
        MenuRespVO::getId,
        MenuRespVO::getParentId,
        MenuRespVO::setChildren,
        -1L  // 自定义根节点父ID
);
```

---

## ⚠️ 注意事项

### 1. VO 类需要有 `children` 字段和 setter

```java
@Data
public class MenuRespVO {
    private Long id;
    private Long parentId;
    private String name;
    
    // 必须有 children 字段和 setter
    private List<MenuRespVO> children;
}
```

### 2. 处理 `parentId` 为 `null` 的情况

工具方法会自动处理：
- 如果 `parentId` 为 `null`，会被视为根节点（相当于 `parentId = rootParentId`）
- 在 `groupingBy` 时会过滤掉 `parentId` 为 `null` 的节点，避免 `NullPointerException`

### 3. 性能考虑

- **时间复杂度**：O(n)，其中 n 是列表长度
- **空间复杂度**：O(n)，需要额外的 Map 存储分组数据
- 适用于中小规模数据（< 10000 条）

### 4. 数据一致性

确保数据的完整性：
- 所有 `parentId` 都应该指向存在的节点ID
- 不应该有循环引用
- 孤儿节点（`parentId` 指向不存在的节点）不会出现在树中

---

## 🎯 优势

1. **通用性强**：使用泛型，适用于任何树形结构
2. **灵活性高**：可以自定义 ID、parentId 的获取方式
3. **易于使用**：静态方法，无需依赖注入
4. **代码复用**：避免在多处重复编写树形结构构建逻辑
5. **NULL 安全**：自动处理 `parentId` 为 `null` 的情况

---

## 💡 未来优化建议

### 建议1：抽取为独立的工具类

如果树形结构转换被广泛使用，可以考虑抽取为独立的工具类：

```java
package com.nexus.backend.admin.common.util;

public class TreeUtils {
    
    public static <T> List<T> buildTree(...) {
        // 当前 MenuService.buildTree() 的实现
    }
    
    // 可以添加更多树相关的工具方法
    public static <T> void traverseTree(...) { }
    
    public static <T> T findNode(...) { }
}
```

### 建议2：支持更多配置选项

```java
// 支持自定义排序
public static <T> List<T> buildTree(
    List<T> list,
    Function<T, Long> getId,
    Function<T, Long> getParentId,
    BiConsumer<T, List<T>> setChildren,
    Long rootParentId,
    Comparator<T> comparator  // 新增：排序器
) { ... }
```

### 建议3：添加缓存支持

对于频繁访问且很少变化的树形数据，可以考虑添加缓存：

```java
@Cacheable(value = "menuTree")
public List<MenuRespVO> getMenuTree() { ... }
```

---

## 📚 相关文档

- [Service 层设计规范](SERVICE_RULE_CHECKLIST.md)
- [代码重构计划](REFACTORING_PLAN.md)

---

**最后更新时间**：2025-10-03

