package com.nexus.backend.admin.controller.user.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户信息表 Response VO
 *
 * @author beckend
 * @since 2025-10-02
 */
@Data
public class UserRespVO {

    /**
     * 用户ID
     */
    @ExcelIgnore
    private Long id;

    /**
     * 用户账号
     */
    @ExcelProperty("用户账号")
    private String username;

    /**
     * 用户昵称
     */
    @ExcelProperty("用户昵称")
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
    @ExcelProperty("用户邮箱")
    private String email;

    /**
     * 手机号码
     */
    @ExcelProperty("手机号码")
    private String mobile;

    /**
     * 用户性别（0=未知 1=男 2=女）
     */
    @ExcelProperty("用户性别")
    private Integer sex;

    /**
     * 头像地址
     */
    @ExcelProperty("头像地址")
    private String avatar;

    /**
     * 帐号状态（0=正常 1=停用）
     */
    @ExcelProperty("帐号状态")
    private Integer status;

    /**
     * 最后登录IP
     */
    @ExcelIgnore
    private String loginIp;

    /**
     * 最后登录时间
     */
    @ExcelIgnore
    private LocalDateTime loginDate;

    /**
     * 创建者
     */
    @ExcelIgnore
    private String creator;

    /**
     * 创建时间
     */
    @ExcelIgnore
    private LocalDateTime dateCreated;

    /**
     * 更新者
     */
    @ExcelIgnore
    private String updater;

    /**
     * 更新时间
     */
    @ExcelIgnore
    private LocalDateTime lastUpdated;

    /**
     * 租户编号
     */
    @ExcelIgnore
    private Long tenantId;

}
