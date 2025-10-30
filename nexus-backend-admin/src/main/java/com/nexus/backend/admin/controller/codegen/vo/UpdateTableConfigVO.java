package com.nexus.backend.admin.controller.codegen.vo;

import com.nexus.backend.admin.dal.dataobject.codegen.CodegenColumnDO;
import com.nexus.backend.admin.dal.dataobject.codegen.CodegenTableDO;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 更新表配置请求
 *
 * @author yourcompany
 * @since 2024-01-01
 */
@Data
public class UpdateTableConfigVO {

    /**
     * 表配置
     */
    @NotNull(message = "表配置不能为空")
    @Valid
    private CodegenTableDO table;

    /**
     * 字段配置列表
     */
    @NotNull(message = "字段配置列表不能为空")
    @Valid
    private List<CodegenColumnDO> columns;

}
