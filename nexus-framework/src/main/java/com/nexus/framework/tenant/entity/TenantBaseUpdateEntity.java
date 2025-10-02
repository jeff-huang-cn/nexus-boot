package com.nexus.framework.tenant.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.nexus.framework.mybatis.entity.BaseCreateEntity;
import com.nexus.framework.mybatis.entity.BaseUpdateEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class TenantBaseUpdateEntity extends BaseUpdateEntity {

    @Serial
    private static final long serialVersionUID = 8889179795023021678L;

    @TableField(value = "tenant_id")
    private Long tenantId;
}
