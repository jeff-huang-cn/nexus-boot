package com.nexus.backend.admin.dal.dataobject.tenant;

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
 * 租户管理表 DO
 *
 * @author beckend
 * @since 2025-10-08
 */
@TableName("system_tenant")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantDO extends BaseDO {

    /**
     * 租户ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 租户名称
     */
    @TableField("name")
    private String name;

    /**
     * 租户编码（用于识别租户）
     */
    @TableField("code")
    private String code;

    /**
     * 数据源ID（关联datasource_config.id）
     */
    @TableField("datasource_id")
    private Long datasourceId;

    /**
     * 分配的菜单ID集合，格式：[1, 2, 100, 101, 102]
     */
    @TableField("menu_ids")
    private String menuIds;

    /**
     * 用户数量（可创建的用户数量）
     */
    @TableField("max_users")
    private Integer maxUsers;

    /**
     * 过期时间
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /**
     * 状态：0-禁用 1-启用
     */
    @TableField("status")
    private Integer status;

}

