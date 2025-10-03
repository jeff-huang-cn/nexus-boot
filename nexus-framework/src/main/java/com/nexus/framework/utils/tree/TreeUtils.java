package com.nexus.framework.utils.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 树形结构工具类
 * 
 * <p>
 * 提供通用的树形结构构建方法，适用于任何需要树形展示的数据
 * </p>
 * 
 * <p>
 * 使用示例：
 * </p>
 * 
 * <pre>
 * List&lt;MenuVO&gt; menuTree = TreeUtils.buildTree(
 *         menuList,
 *         MenuVO::getId,
 *         MenuVO::getParentId,
 *         MenuVO::setChildren,
 *         0L);
 * </pre>
 *
 * @author nexus
 */
public class TreeUtils {

    /**
     * 构建树形结构（通用方法）
     * 
     * <p>
     * 将扁平列表转换为树形结构。支持任何具有 id 和 parentId 的数据类型。
     * </p>
     * 
     * <p>
     * 特性：
     * </p>
     * <ul>
     * <li>自动处理 parentId 为 null 的节点（视为根节点）</li>
     * <li>避免 NullPointerException</li>
     * <li>性能优化：使用 groupingBy 预分组，时间复杂度 O(n)</li>
     * </ul>
     * 
     * @param <T>          树节点类型
     * @param list         扁平列表
     * @param getId        获取节点ID的函数（如：MenuVO::getId）
     * @param getParentId  获取父节点ID的函数（如：MenuVO::getParentId）
     * @param setChildren  设置子节点的函数（如：MenuVO::setChildren）
     * @param rootParentId 根节点的父ID（通常为 0L）
     * @return 树形结构的根节点列表
     */
    public static <T> List<T> buildTree(List<T> list,
            Function<T, Long> getId,
            Function<T, Long> getParentId,
            BiConsumer<T, List<T>> setChildren,
            Long rootParentId) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }

        List<T> result = new ArrayList<>();

        // 过滤掉 parentId 为 null 的节点，并按父ID分组
        // 这样可以避免 NullPointerException
        Map<Long, List<T>> parentMap = list.stream()
                .filter(item -> getParentId.apply(item) != null)
                .collect(Collectors.groupingBy(getParentId));

        // 遍历所有节点，构建树形结构
        for (T item : list) {
            // 处理 parentId 为 null 的情况，视为顶级节点
            Long parentId = getParentId.apply(item);
            Long actualParentId = parentId != null ? parentId : rootParentId;

            // 如果是根节点
            if (actualParentId.equals(rootParentId)) {
                // 获取该节点的所有子节点
                List<T> children = parentMap.get(getId.apply(item));
                if (children != null && !children.isEmpty()) {
                    setChildren.accept(item, children);
                }
                result.add(item);
            }
        }

        return result;
    }

    /**
     * 遍历树形结构（深度优先）
     * 
     * @param <T>         树节点类型
     * @param tree        树形结构
     * @param getChildren 获取子节点的函数
     * @param consumer    对每个节点执行的操作
     */
    public static <T> void traverseTree(List<T> tree,
            Function<T, List<T>> getChildren,
            java.util.function.Consumer<T> consumer) {
        if (tree == null || tree.isEmpty()) {
            return;
        }

        for (T node : tree) {
            // 先处理当前节点
            consumer.accept(node);

            // 再递归处理子节点
            List<T> children = getChildren.apply(node);
            if (children != null && !children.isEmpty()) {
                traverseTree(children, getChildren, consumer);
            }
        }
    }

    /**
     * 在树中查找节点
     * 
     * @param <T>         树节点类型
     * @param tree        树形结构
     * @param getChildren 获取子节点的函数
     * @param predicate   查找条件
     * @return 找到的节点，未找到返回 null
     */
    public static <T> T findNode(List<T> tree,
            Function<T, List<T>> getChildren,
            java.util.function.Predicate<T> predicate) {
        if (tree == null || tree.isEmpty()) {
            return null;
        }

        for (T node : tree) {
            // 检查当前节点是否符合条件
            if (predicate.test(node)) {
                return node;
            }

            // 在子节点中查找
            List<T> children = getChildren.apply(node);
            if (children != null && !children.isEmpty()) {
                T found = findNode(children, getChildren, predicate);
                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    /**
     * 将树形结构扁平化为列表
     * 
     * @param <T>         树节点类型
     * @param tree        树形结构
     * @param getChildren 获取子节点的函数
     * @return 扁平化后的列表
     */
    public static <T> List<T> flattenTree(List<T> tree,
            Function<T, List<T>> getChildren) {
        List<T> result = new ArrayList<>();

        if (tree == null || tree.isEmpty()) {
            return result;
        }

        for (T node : tree) {
            result.add(node);

            List<T> children = getChildren.apply(node);
            if (children != null && !children.isEmpty()) {
                result.addAll(flattenTree(children, getChildren));
            }
        }

        return result;
    }

    /**
     * 过滤树形结构
     * 
     * <p>
     * 注意：如果父节点不满足条件，其子节点也会被过滤掉
     * </p>
     * 
     * @param <T>         树节点类型
     * @param tree        树形结构
     * @param getChildren 获取子节点的函数
     * @param setChildren 设置子节点的函数
     * @param predicate   过滤条件
     * @return 过滤后的树形结构
     */
    public static <T> List<T> filterTree(List<T> tree,
            Function<T, List<T>> getChildren,
            BiConsumer<T, List<T>> setChildren,
            java.util.function.Predicate<T> predicate) {
        if (tree == null || tree.isEmpty()) {
            return new ArrayList<>();
        }

        List<T> result = new ArrayList<>();

        for (T node : tree) {
            // 如果当前节点满足条件
            if (predicate.test(node)) {
                // 递归过滤子节点
                List<T> children = getChildren.apply(node);
                if (children != null && !children.isEmpty()) {
                    List<T> filteredChildren = filterTree(children, getChildren, setChildren, predicate);
                    setChildren.accept(node, filteredChildren);
                }
                result.add(node);
            }
        }

        return result;
    }

    /**
     * 私有构造函数，防止实例化
     */
    private TreeUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
