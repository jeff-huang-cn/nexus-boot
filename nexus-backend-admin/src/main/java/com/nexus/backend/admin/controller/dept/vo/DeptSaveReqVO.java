package com.nexus.backend.admin.controller.dept.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 部门管理表 新增/修改 Request VO
 *
 * @author beckend
 * @since 2025-10-28
 */
@Schema(description = "部门管理表 新增/修改 Request VO")
@Data
public class DeptSaveReqVO {

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "部门名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "示例名称")
    @ExcelProperty("部门名称")
    @NotBlank(message = "部门名称不能为空")
    private String name;

    @Schema(description = "部门编码", example = "示例文本")
    @ExcelProperty("部门编码")
    private String code;

    @Schema(description = "父部门ID（0表示根部门）", example = "1")
    @ExcelProperty("父部门ID（0表示根部门）")
    private Long parentId;

    @Schema(description = "显示顺序", example = "1")
    @ExcelProperty("显示顺序")
    private Integer sort;

    @Schema(description = "负责人ID", example = "1")
    @ExcelProperty("负责人ID")
    private Long leaderUserId;

    @Schema(description = "联系电话", example = "13888888888")
    @ExcelProperty("联系电话")
    private String phone;

    @Schema(description = "邮箱", example = "example@email.com")
    @ExcelProperty("邮箱")
    private String email;

    @Schema(description = "状态：0-禁用 1-启用", example = "1")
    @ExcelProperty("状态：0-禁用 1-启用")
    private Integer status;

}

