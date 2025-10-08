package com.nexus.backend.admin.controller.tenant.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 租户管理表 Response VO
 *
 * @author beckend
 * @since 2025-10-08
 */
@Schema(description = "租户管理表 Response VO")
@Data
public class TenantRespVO {

    @Schema(description = "ID", example = "1")
    @ExcelIgnore
    private Long id;

    @Schema(description = "租户名称", example = "示例名称")
    @ExcelProperty("租户名称")
    private String name;

    @Schema(description = "租户编码（用于识别租户）", example = "示例文本")
    @ExcelProperty("租户编码")
    private String code;

    @Schema(description = "数据源ID（关联datasource_config.id）", example = "1")
    @ExcelProperty("数据源ID")
    private Long datasourceId;

    @Schema(description = "分配的菜单ID集合，格式：[1, 2, 100, 101, 102]", example = "示例文本")
    @ExcelProperty("分配的菜单ID集合")
    private String menuIds;

    @Schema(description = "用户数量（可创建的用户数量）", example = "1")
    @ExcelProperty("用户数量")
    private Integer maxUsers;

    @Schema(description = "过期时间", example = "2024-01-01 00:00:00")
    @ExcelProperty("过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "状态：0-禁用 1-启用", example = "1")
    @ExcelProperty("状态")
    private Integer status;

    @Schema(description = "创建人", example = "示例文本")
    @ExcelIgnore
    private String creator;

    @Schema(description = "创建时间", example = "2024-01-01 00:00:00")
    @ExcelIgnore
    private LocalDateTime dateCreated;

    @Schema(description = "更新人", example = "示例文本")
    @ExcelIgnore
    private String updater;

    @Schema(description = "更新时间", example = "2024-01-01 00:00:00")
    @ExcelIgnore
    private LocalDateTime lastUpdated;

}

