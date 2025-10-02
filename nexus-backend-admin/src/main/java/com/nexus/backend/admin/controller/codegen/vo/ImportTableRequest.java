package com.nexus.backend.admin.controller.codegen.vo;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 导入表请求
 *
 * @author yourcompany
 * @since 2024-01-01
 */
@Data
public class ImportTableRequest {

    /**
     * 表名列表
     */
    @NotEmpty(message = "表名列表不能为空")
    private List<String> tableNames;

    /**
     * 数据源配置ID（可选，默认使用主数据源）
     */
    private Long datasourceConfigId = 1L;

}
