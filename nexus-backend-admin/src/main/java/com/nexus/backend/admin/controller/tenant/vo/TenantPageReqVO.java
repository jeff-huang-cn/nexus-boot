package com.nexus.backend.admin.controller.tenant.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.nexus.framework.mybatis.entity.BasePageQuery;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.nexus.framework.utils.date.DateUtils.DATETIME_PATTERN;

/**
 * 租户管理表 分页 Request VO
 *
 * @author beckend
 * @since 2025-10-08
 */
@Schema(description = "租户管理表 Page Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class TenantPageReqVO extends BasePageQuery {

    @Schema(description = "租户ID", example = "1")
    private Long id;
    @Schema(description = "租户名称", example = "示例名称")
    private String name;
    @Schema(description = "租户编码（用于识别租户）", example = "示例文本")
    private String code;
    @Schema(description = "数据源ID（关联datasource_config.id）", example = "1")
    private Long datasourceId;
    @Schema(description = "分配的菜单ID集合，格式：[1, 2, 100, 101, 102]", example = "示例文本")
    private String menuIds;
    @Schema(description = "用户数量（可创建的用户数量）", example = "1")
    private Integer maxUsers;
    @Schema(description = "过期时间 - 开始时间", example = "2023-01-01 00:00:00")
    @DateTimeFormat(pattern = DATETIME_PATTERN)
    private LocalDateTime expireTimeStart;

    @Schema(description = "过期时间 - 结束时间", example = "2023-01-31 23:59:59")
    @DateTimeFormat(pattern = DATETIME_PATTERN)
    private LocalDateTime expireTimeEnd;
    @Schema(description = "状态：0-禁用 1-启用", example = "1")
    private Integer status;
    @Schema(description = "创建人", example = "示例文本")
    private String creator;
    @Schema(description = "创建时间 - 开始时间", example = "2023-01-01 00:00:00")
    @DateTimeFormat(pattern = DATETIME_PATTERN)
    private LocalDateTime dateCreatedStart;

    @Schema(description = "创建时间 - 结束时间", example = "2023-01-31 23:59:59")
    @DateTimeFormat(pattern = DATETIME_PATTERN)
    private LocalDateTime dateCreatedEnd;
    @Schema(description = "更新人", example = "示例文本")
    private String updater;
    @Schema(description = "更新时间 - 开始时间", example = "2023-01-01 00:00:00")
    @DateTimeFormat(pattern = DATETIME_PATTERN)
    private LocalDateTime lastUpdatedStart;

    @Schema(description = "更新时间 - 结束时间", example = "2023-01-31 23:59:59")
    @DateTimeFormat(pattern = DATETIME_PATTERN)
    private LocalDateTime lastUpdatedEnd;
}
