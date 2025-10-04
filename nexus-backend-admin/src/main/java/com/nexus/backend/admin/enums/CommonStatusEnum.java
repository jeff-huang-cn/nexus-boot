package com.nexus.backend.admin.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用状态枚举
 * 用于表示启用/禁用状态，比 status 字段更直观
 *
 * @author nexus
 */
@Getter
@AllArgsConstructor
public enum CommonStatusEnum {

    /**
     * 禁用
     */
    DISABLE(0, "禁用"),

    /**
     * 启用
     */
    ENABLE(1, "启用");

    /**
     * 状态值
     */
    private final Integer value;

    /**
     * 状态名称
     */
    private final String label;

    /**
     * 根据值获取枚举
     *
     * @param value 状态值
     * @return 枚举，如果找不到返回 null
     */
    public static CommonStatusEnum valueOf(Integer value) {
        if (value == null) {
            return null;
        }
        for (CommonStatusEnum status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否为启用状态
     *
     * @param value 状态值
     * @return true=启用，false=禁用或null
     */
    public static boolean isEnable(Integer value) {
        return ENABLE.value.equals(value);
    }

    /**
     * 判断是否为禁用状态
     *
     * @param value 状态值
     * @return true=禁用，false=启用或null
     */
    public static boolean isDisable(Integer value) {
        return DISABLE.value.equals(value);
    }
}
