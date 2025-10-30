package com.nexus.backend.admin.dal.dataobject.permission;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.nexus.framework.mybatis.entity.BaseCreateDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户角色关联 DO
 *
 * @author nexus
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("system_user_role")
public class UserRoleDO extends BaseCreateDO {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色ID
     */
    private Long roleId;
}
