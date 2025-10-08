package com.nexus.backend.admin.controller.user.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息表保存 Request VO（新增/修改）
 *
 * @author beckend
 * @since 2025-10-02
 */
@Data
public class UserSaveReqVO {

    /**
     * 编号
     */
    @ExcelIgnore
    private Long id;

    /**
     * 用户账号
     */
    @NotBlank(message = "用户账号不能为空")
    @ExcelProperty("用户账号")
    private String username;

    /**
     * 密码
     */
    @ExcelIgnore
    private String password;

    /**
     * 用户昵称
     */
    @NotBlank(message = "用户昵称不能为空")
    @ExcelProperty("用户昵称")
    private String nickname;

    /**
     * 备注
     */
    @ExcelIgnore
    private String remark;

    /**
     * 部门ID
     */
    @ExcelIgnore
    private Long deptId;

    /**
     * 岗位编号数组
     */
    @ExcelIgnore
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
    @NotNull(message = "帐号状态（0=正常 1=停用）不能为空")
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

}
