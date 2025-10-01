package com.nexus.backend.admin.modules.system.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 系统用户更新DTO
 *
 * @author beckend
 * @since 2024-01-01
 */
@Data
public class SystemUserUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long id;

    /**
     * 用户昵称
     */
    @Size(max = 30, message = "用户昵称不能超过30个字符")
    private String nickname;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注不能超过500个字符")
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
    @Email(message = "邮箱格式不正确")
    @Size(max = 50, message = "邮箱不能超过50个字符")
    private String email;

    /**
     * 手机号码
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
    private String mobile;

    /**
     * 用户性别（0男 1女 2未知）
     */
    private Integer sex;

    /**
     * 头像地址
     */
    @Size(max = 512, message = "头像地址不能超过512个字符")
    private String avatar;

    /**
     * 账号状态（0正常 1停用）
     */
    private Integer status;
}
