package com.nexus.backend.admin.controller.tenant.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 租户管理表 新增/修改 Request VO
 *
 * @author beckend
 * @since 2025-10-08
 */
@Schema(description = "租户管理表 新增/修改 Request VO")
@Data
public class TenantSaveReqVO {

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "租户名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "示例名称")
    @ExcelProperty("租户名称")
    @NotBlank(message = "租户名称不能为空")
    private String name;

    @Schema(description = "租户编码（用于识别租户）", example = "示例文本")
    @ExcelProperty("租户编码（用于识别租户）")
    private String code;

    @Schema(description = "数据源ID（关联datasource_config.id）", example = "1")
    @ExcelProperty("数据源ID（关联datasource_config.id）")
    private Long datasourceId;

    @Schema(description = "分配的菜单ID集合，格式：[1, 2, 100, 101, 102]", example = "示例文本")
    @ExcelProperty("分配的菜单ID集合，格式：[1, 2, 100, 101, 102]")
    private String menuIds;

    @Schema(description = "用户数量（可创建的用户数量）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("用户数量（可创建的用户数量）")
    @NotNull(message = "用户数量（可创建的用户数量）不能为空")
    private Integer maxUsers;

    @Schema(description = "过期时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-01-01 00:00:00")
    @ExcelProperty("过期时间")
    @NotNull(message = "过期时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expireTime;

    @Schema(description = "状态：0-禁用 1-启用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("状态：0-禁用 1-启用")
    @NotNull(message = "状态：0-禁用 1-启用不能为空")
    private Integer status;

}
