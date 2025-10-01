package com.nexus.backend.admin.modules.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nexus.framework.mybatis.entity.BaseVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统用户VO
 *
 * @author beckend
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SystemUserVO extends BaseVO {

    private static final long serialVersionUID = 1L;

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
     * 部门名称（关联查询）
     */
    private String deptName;

    /**
     * 岗位编号数组
     */
    private String postIds;

    /**
     * 岗位名称（关联查询）
     */
    private String postNames;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String mobile;

    /**
     * 用户性别（0男 1女 2未知）
     */
    private Integer sex;

    /**
     * 用户性别描述
     */
    private String sexText;

    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 账号状态（0正常 1停用）
     */
    private Integer status;

    /**
     * 账号状态描述
     */
    private String statusText;

    /**
     * 最后登录IP
     */
    private String loginIp;

    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginDate;

    /**
     * 租户编号
     */
    private Long tenantId;

    public String getSexText() {
        if (sex == null)
            return "未知";
        switch (sex) {
            case 0:
                return "男";
            case 1:
                return "女";
            default:
                return "未知";
        }
    }

    public String getStatusText() {
        if (status == null)
            return "未知";
        switch (status) {
            case 0:
                return "正常";
            case 1:
                return "停用";
            default:
                return "未知";
        }
    }
}
