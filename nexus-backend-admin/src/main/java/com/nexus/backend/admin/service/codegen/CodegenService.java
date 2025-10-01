package com.nexus.backend.admin.service.codegen;

import com.nexus.backend.admin.common.result.PageResult;
import com.nexus.backend.admin.entity.codegen.CodegenColumn;
import com.nexus.backend.admin.entity.codegen.CodegenTable;

import java.util.List;
import java.util.Map;

/**
 * 代码生成服务
 *
 * @author yourcompany
 * @since 2024-01-01
 */
public interface CodegenService {

    /**
     * 查询代码生成表列表
     *
     * @param current 当前页
     * @param size 每页条数
     * @param tableName 表名
     * @param tableComment 表注释
     * @return 分页结果
     */
    PageResult<CodegenTable> getTableList(Long current, Long size, String tableName, String tableComment);

    /**
     * 根据ID查询代码生成表信息
     *
     * @param id 表ID
     * @return 表信息
     */
    CodegenTable getTableById(Long id);

    /**
     * 根据表ID查询字段列表
     *
     * @param tableId 表ID
     * @return 字段列表
     */
    List<CodegenColumn> getColumnsByTableId(Long tableId);

    /**
     * 导入数据库表
     *
     * @param datasourceConfigId 数据源配置ID
     * @param tableNames 表名列表
     * @return 导入的表ID列表
     */
    List<Long> importTables(Long datasourceConfigId, List<String> tableNames);

    /**
     * 更新代码生成表配置
     *
     * @param table 表配置
     * @param columns 字段配置列表
     */
    void updateTableConfig(CodegenTable table, List<CodegenColumn> columns);

    /**
     * 删除代码生成表
     *
     * @param id 表ID
     */
    void deleteTable(Long id);

    /**
     * 预览生成代码
     *
     * @param tableId 表ID
     * @return 预览代码Map，key为文件名，value为代码内容
     */
    Map<String, String> previewCode(Long tableId);

    /**
     * 生成代码
     *
     * @param tableId 表ID
     * @return 生成的代码文件字节数组（ZIP格式）
     */
    byte[] generateCode(Long tableId);

    /**
     * 批量生成代码
     *
     * @param tableIds 表ID列表
     * @return 生成的代码文件字节数组（ZIP格式）
     */
    byte[] batchGenerateCode(List<Long> tableIds);

}
