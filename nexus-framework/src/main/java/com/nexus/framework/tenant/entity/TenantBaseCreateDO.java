package com.nexus.framework.tenant.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.nexus.framework.mybatis.entity.BaseCreateDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
public class TenantBaseCreateDO extends BaseCreateDO {

    @Serial
    private static final long serialVersionUID = 5395666692573184417L;

    @TableField(value = "tenant_id")
    private Long tenantId;
}
