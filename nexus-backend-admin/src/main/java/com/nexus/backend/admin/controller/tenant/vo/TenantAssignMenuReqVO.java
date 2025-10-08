package com.nexus.backend.admin.controller.tenant.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 租户分配菜单 Request VO
 *
 * @author beckend
 * @since 2025-10-08
 */
@Schema(description = "租户分配菜单 Request VO")
@Data
public class TenantAssignMenuReqVO {

    @Schema(description = "租户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "租户ID不能为空")
    private Long tenantId;

    @Schema(description = "菜单ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "菜单ID列表不能为空")
    private List<Long> menuIds;

}
