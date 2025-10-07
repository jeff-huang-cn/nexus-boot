package com.nexus.framework.tenant.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.nexus.framework.mybatis.entity.BaseUpdateDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
public class TenantBaseUpdateDO extends BaseUpdateDO {

    @Serial
    private static final long serialVersionUID = 8889179795023021678L;

    @TableField(value = "tenant_id")
    private Long tenantId;
}
