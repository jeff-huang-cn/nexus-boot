package com.nexus.backend.admin.controller.user.vo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 个人信息更新请求VO
 *
 * @author nexus
 */
@Data
public class ProfileUpdateReqVO {

    /**
     * 用户昵称
     */
    @NotBlank(message = "昵称不能为空")
    private String nickname;

    /**
     * 用户邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号码
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;

    /**
     * 用户性别（0=未知，1=男，2=女）
     */
    private Integer sex;

    /**
     * 头像地址
     */
    private String avatar;
}
