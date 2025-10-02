package com.nexus.framework.tenant.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.nexus.framework.mybatis.entity.BaseEntity;
import com.nexus.framework.mybatis.entity.BaseUpdateEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = false)
public class TenantBaseEntity extends BaseEntity {
    @Serial
    private static final long serialVersionUID = -2866430410247320282L;

    @TableField(value = "tenant_id")
    private Long tenantId;
}
