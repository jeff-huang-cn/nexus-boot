package com.nexus.backend.admin.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 菜单类型枚举
 *
 * @author nexus
 */
@Getter
@AllArgsConstructor
public enum MenuTypeEnum {

    /**
     * 目录
     * 用于组织菜单的顶层节点，通常不关联具体页面
     */
    DIR(1, "目录"),

    /**
     * 菜单
     * 关联具体的页面路由
     */
    MENU(2, "菜单"),

    /**
     * 按钮
     * 页面上的操作按钮，关联具体的权限标识
     * 例如：创建、编辑、删除、导出等按钮
     */
    BUTTON(3, "按钮");

    /**
     * 类型值
     */
    private final Integer value;

    /**
     * 类型名称
     */
    private final String label;

    /**
     * 根据值获取枚举
     *
     * @param value 类型值
     * @return 枚举，如果找不到返回 null
     */
    public static MenuTypeEnum valueOf(Integer value) {
        if (value == null) {
            return null;
        }
        for (MenuTypeEnum type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 判断是否为目录
     *
     * @param value 类型值
     * @return true=目录，false=其他
     */
    public static boolean isDir(Integer value) {
        return DIR.value.equals(value);
    }

    /**
     * 判断是否为菜单
     *
     * @param value 类型值
     * @return true=菜单，false=其他
     */
    public static boolean isMenu(Integer value) {
        return MENU.value.equals(value);
    }

    /**
     * 判断是否为按钮
     *
     * @param value 类型值
     * @return true=按钮，false=其他
     */
    public static boolean isButton(Integer value) {
        return BUTTON.value.equals(value);
    }
}
