package com.nexus.backend.admin.dal.dataobject.user;

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
 * 用户信息表 DO
 *
 * @author beckend
 * @since 2025-10-02
 */
@TableName("system_user")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDO extends BaseDO {

    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户账号
     */
    @TableField("username")
    private String username;

    /**
     * 密码
     */
    @TableField("password")
    private String password;

    /**
     * 用户昵称
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 部门ID
     */
    @TableField("dept_id")
    private Long deptId;

    /**
     * 岗位编号数组
     */
    @TableField("post_ids")
    private String postIds;

    /**
     * 用户邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 手机号码
     */
    @TableField("mobile")
    private String mobile;

    /**
     * 用户性别（0=未知 1=男 2=女）
     */
    @TableField("sex")
    private Integer sex;

    /**
     * 头像地址
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 帐号状态（0=正常 1=停用）
     */
    @TableField("status")
    private Integer status;

    /**
     * 最后登录IP
     */
    @TableField("login_ip")
    private String loginIp;

    /**
     * 最后登录时间
     */
    @TableField("login_date")
    private LocalDateTime loginDate;

}

