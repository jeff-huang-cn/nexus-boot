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
     * 账户过期时间（NULL=永不过期，当前时间>此值=账户已过期）
     */
    @TableField("expired_time")
    private LocalDateTime expiredTime;

    /**
     * 账户锁定截止时间（NULL=未锁定，当前时间<此值=账户被锁定）
     * 适用于：密码错误多次后锁定 1 小时等场景
     */
    @TableField("locked_time")
    private LocalDateTime lockedTime;

    /**
     * 密码过期时间（NULL=永不过期，当前时间>此值=密码已过期）
     */
    @TableField("password_expire_time")
    private LocalDateTime passwordExpireTime;

    /**
     * 密码最后修改时间
     */
    @TableField("password_update_time")
    private LocalDateTime passwordUpdateTime;

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

    // ==================== 业务方法：用户状态判断 ====================

    /**
     * 判断用户是否已启用
     * 
     * @return true=已启用，false=已停用
     */
    public boolean isEnabled() {
        // status: 0=正常 1=停用
        return this.status != null && this.status == 0;
    }

    /**
     * 判断账户是否未过期
     * 
     * @return true=未过期，false=已过期
     */
    public boolean isAccountNonExpired() {
        // NULL 表示永不过期
        if (this.expiredTime == null) {
            return true;
        }
        // 当前时间 <= 过期时间 => 未过期
        LocalDateTime now = LocalDateTime.now();
        return now.isBefore(this.expiredTime) || now.isEqual(this.expiredTime);
    }

    /**
     * 判断账户是否未锁定
     * 
     * @return true=未锁定，false=已锁定
     */
    public boolean isAccountNonLocked() {
        // NULL 表示未锁定
        if (this.lockedTime == null) {
            return true;
        }
        // 当前时间 >= 锁定截止时间 => 锁定已解除
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(this.lockedTime) || now.isEqual(this.lockedTime);
    }

    /**
     * 判断凭证（密码）是否未过期
     * 
     * @return true=未过期，false=已过期
     */
    public boolean isCredentialsNonExpired() {
        // NULL 表示永不过期
        if (this.passwordExpireTime == null) {
            return true;
        }
        // 当前时间 <= 密码过期时间 => 未过期
        LocalDateTime now = LocalDateTime.now();
        return now.isBefore(this.passwordExpireTime) || now.isEqual(this.passwordExpireTime);
    }

    /**
     * 综合判断用户账户是否可用
     * 必须同时满足：已启用、未过期、未锁定、密码未过期
     * 
     * @return true=可用，false=不可用
     */
    public boolean isAccountAvailable() {
        return isEnabled()
                && isAccountNonExpired()
                && isAccountNonLocked()
                && isCredentialsNonExpired();
    }

    /**
     * 获取账户不可用的原因
     * 
     * @return 不可用原因，如果账户可用则返回 null
     */
    public String getUnavailableReason() {
        if (!isEnabled()) {
            return "账户已被停用";
        }
        if (!isAccountNonExpired()) {
            return "账户已过期";
        }
        if (!isAccountNonLocked()) {
            return "账户已被锁定";
        }
        if (!isCredentialsNonExpired()) {
            return "密码已过期，请修改密码";
        }
        return null;
    }

    // ==================== 业务方法：状态管理 ====================

    /**
     * 锁定账户（设置锁定截止时间）
     * 
     * @param durationMinutes 锁定时长（分钟）
     * @return 锁定截止时间
     */
    public LocalDateTime lockAccount(int durationMinutes) {
        LocalDateTime lockedTime = LocalDateTime.now().plusMinutes(durationMinutes);
        this.lockedTime = lockedTime;
        return lockedTime;
    }

    /**
     * 解锁账户（清除锁定时间）
     */
    public void unlockAccount() {
        this.lockedTime = null;
    }

    /**
     * 设置账户过期时间
     * 
     * @param expiredTime 过期时间（NULL=永不过期）
     */
    public void setAccountExpireTime(LocalDateTime expiredTime) {
        this.expiredTime = expiredTime;
    }

    /**
     * 设置密码过期时间
     * 
     * @param durationDays 有效天数（从当前时间开始计算）
     * @return 密码过期时间
     */
    public LocalDateTime setPasswordExpireTime(int durationDays) {
        LocalDateTime expireTime = LocalDateTime.now().plusDays(durationDays);
        this.passwordExpireTime = expireTime;
        this.passwordUpdateTime = LocalDateTime.now();
        return expireTime;
    }

}
