package com.nexus.backend.admin.controller.codegen.dto;

import com.nexus.backend.admin.entity.codegen.CodegenColumn;
import com.nexus.backend.admin.entity.codegen.CodegenTable;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 更新表配置请求DTO
 *
 * @author yourcompany
 * @since 2024-01-01
 */
@Data
public class UpdateTableConfigDTO {

    /**
     * 表配置
     */
    @Valid
    @NotNull(message = "表配置不能为空")
    private CodegenTable table;

    /**
     * 字段配置列表
     */
    @Valid
    private List<CodegenColumn> columns;

}
