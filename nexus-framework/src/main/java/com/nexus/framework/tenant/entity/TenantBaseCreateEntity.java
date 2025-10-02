package com.nexus.framework.tenant.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.nexus.framework.mybatis.entity.BaseCreateEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class TenantBaseCreateEntity extends BaseCreateEntity {

    @Serial
    private static final long serialVersionUID = 5395666692573184417L;

    @TableField(value = "tenant_id")
    private Long tenantId;
}
