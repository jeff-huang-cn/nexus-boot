package com.nexus.backend.admin.dal.dataobject.permission;

import com.baomidou.mybatisplus.annotation.*;
import com.nexus.framework.mybatis.entity.BaseDO;
import com.nexus.framework.mybatis.entity.BaseUpdateEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统菜单 DO
 *
 * @author nexus
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("system_menu")
public class MenuDO extends BaseUpdateEntity {

    /**
     * 菜单ID
     */
    @TableId(type = IdType.AUTO)
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

}
