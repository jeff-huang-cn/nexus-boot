package com.nexus.backend.admin.dal.dataobject.dept;

import com.baomidou.mybatisplus.annotation.*;
import com.nexus.framework.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 部门管理表 DO
 *
 * @author beckend
 * @since 2025-10-28
 */
@TableName("system_dept")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeptDO extends BaseDO {

    /**
     * 部门ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 部门名称
     */
    @TableField("name")
    private String name;

    /**
     * 部门编码
     */
    @TableField("code")
    private String code;

    /**
     * 父部门ID（0表示根部门）
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 显示顺序
     */
    @TableField("sort")
    private Integer sort;

    /**
     * 负责人ID
     */
    @TableField("leader_user_id")
    private Long leaderUserId;

    /**
     * 联系电话
     */
    @TableField("phone")
    private String phone;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 状态：0-禁用 1-启用
     */
    @TableField("status")
    private Integer status;

}

