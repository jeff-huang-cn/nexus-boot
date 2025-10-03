package com.nexus.backend.admin.dal.dataobject.permission;

import com.baomidou.mybatisplus.annotation.*;
import com.nexus.framework.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统角色 DO
 *
 * @author nexus
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("system_role")
public class RoleDO extends BaseDO {

    /**
     * 角色ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色编码
     */
    private String code;

    /**
     * 显示顺序
     */
    private Integer sort;

    /**
     * 角色状态：0-禁用 1-启用
     */
    private Integer status;

    /**
     * 角色类型：1-系统内置 2-自定义
     */
    private Integer type;

    /**
     * 备注
     */
    private String remark;

}
