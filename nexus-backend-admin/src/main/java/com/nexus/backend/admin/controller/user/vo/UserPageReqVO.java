package com.nexus.backend.admin.controller.user.vo;

import com.nexus.framework.mybatis.entity.BasePageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 用户信息表分页查询 Request VO
 *
 * @author beckend
 * @since 2025-10-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserPageReqVO extends BasePageQuery {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户账号
     */
    private String username;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 备注
     */
    private String remark;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 岗位编号数组
     */
    private String postIds;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String mobile;

    /**
     * 用户性别（0=未知 1=男 2=女）
     */
    private Integer sex;

    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 帐号状态（0=正常 1=停用）
     */
    private Integer status;

    /**
     * 最后登录IP
     */
    private String loginIp;

    /**
     * 最后登录时间（开始）
     */
    private LocalDateTime loginDateStart;

    /**
     * 最后登录时间（结束）
     */
    private LocalDateTime loginDateEnd;

    /**
     * 创建者
     */
    private String creator;

    /**
     * 创建时间（开始）
     */
    private LocalDateTime dateCreatedStart;

    /**
     * 创建时间（结束）
     */
    private LocalDateTime dateCreatedEnd;

    /**
     * 更新者
     */
    private String updater;

    /**
     * 更新时间（开始）
     */
    private LocalDateTime lastUpdatedStart;

    /**
     * 更新时间（结束）
     */
    private LocalDateTime lastUpdatedEnd;

    /**
     * 租户编号
     */
    private Long tenantId;

}

