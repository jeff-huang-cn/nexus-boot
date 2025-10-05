package com.nexus.backend.admin.controller.user.vo;

import lombok.Data;

/**
 * 个人信息响应VO
 *
 * @author nexus
 */
@Data
public class ProfileRespVO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 手机号码
     */
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
