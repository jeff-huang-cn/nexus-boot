package com.nexus.backend.admin.controller.codegen.vo;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 导入表请求DTO
 *
 * @author yourcompany
 * @since 2024-01-01
 */
@Data
public class ImportTableVO {

    /**
     * 数据源配置ID
     */
    @NotNull(message = "数据源配置ID不能为空")
    private Long datasourceConfigId;

    /**
     * 表名列表
     */
    @NotEmpty(message = "请选择要导入的表")
    private List<String> tableNames;

}
