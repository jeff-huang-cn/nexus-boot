package com.nexus.backend.admin.controller.permission.vo.menu;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 菜单保存请求 VO
 *
 * @author nexus
 */
@Data
public class MenuSaveReqVO {

    /**
     * 菜单ID（更新时需要）
     */
    private Long id;

    /**
     * 菜单名称
     */
    @NotBlank(message = "菜单名称不能为空")
    private String name;

    /**
     * 权限标识
     */
    private String permission;

    /**
     * 菜单类型：1-目录 2-菜单 3-按钮
     */
    @NotNull(message = "菜单类型不能为空")
    private Integer type;

    /**
     * 显示顺序
     */
    private Integer sort;

    /**
     * 父菜单ID
     */
    @NotNull(message = "父菜单ID不能为空")
    private Long parentId;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 组件名称
     */
    private String componentName;

    /**
     * 菜单状态：0-禁用 1-启用
     */
    private Integer status;

    /**
     * 是否可见：0-隐藏 1-显示
     */
    private Integer visible;

    /**
     * 是否缓存：0-不缓存 1-缓存
     */
    private Integer keepAlive;

    /**
     * 是否总是显示：0-否 1-是
     */
    private Integer alwaysShow;

}
