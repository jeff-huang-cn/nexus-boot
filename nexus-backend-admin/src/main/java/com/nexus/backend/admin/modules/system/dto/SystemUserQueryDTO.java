package com.nexus.backend.admin.modules.system.dto;

import com.nexus.framework.mybatis.entity.BasePageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统用户查询DTO
 *
 * @author beckend
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SystemUserQueryDTO extends BasePageQuery {

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
     * 账号状态（0正常 1停用）
     */
    private Integer status;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 创建时间范围 - 开始时间
     */
    private String createTimeStart;

    /**
     * 创建时间范围 - 结束时间
     */
    private String createTimeEnd;
}
