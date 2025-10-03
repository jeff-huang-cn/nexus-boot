package com.nexus.backend.admin.controller.permission.vo.menu;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单响应 VO
 *
 * @author nexus
 */
@Data
public class MenuRespVO {

    /**
     * 菜单ID
     */
    private Long id;

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 权限标识
     */
    private String permission;

    /**
     * 菜单类型：1-目录 2-菜单 3-按钮
     */
    private Integer type;

    /**
     * 显示顺序
     */
    private Integer sort;

    /**
     * 父菜单ID
     */
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

    /**
     * 创建时间
     */
    private LocalDateTime dateCreated;

    /**
     * 子菜单列表（用于树形结构）
     */
    private List<MenuRespVO> children;

}
