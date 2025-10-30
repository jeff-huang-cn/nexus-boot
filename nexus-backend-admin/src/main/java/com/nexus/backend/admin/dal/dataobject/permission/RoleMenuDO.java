package com.nexus.backend.admin.dal.dataobject.permission;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.nexus.framework.mybatis.entity.BaseCreateDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色菜单关联 DO
 *
 * @author nexus
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("system_role_menu")
public class RoleMenuDO extends BaseCreateDO {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 菜单ID
     */
    private Long menuId;

}
