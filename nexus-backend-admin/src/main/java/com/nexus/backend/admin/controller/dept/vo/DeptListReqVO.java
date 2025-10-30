package com.nexus.backend.admin.controller.dept.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.nexus.framework.utils.date.DateUtils.DATETIME_PATTERN;

/**
 * 部门管理表 列表 Request VO
 *
 * @author beckend
 * @since 2025-10-28
 */
@Schema(description = "部门管理表 List Request VO")
@Data
public class DeptListReqVO {

    @Schema(description = "部门名称", example = "示例名称")
    private String name;
    @Schema(description = "部门编码", example = "示例文本")
    private String code;
    @Schema(description = "状态：0-禁用 1-启用", example = "1")
    private Integer status;
}

