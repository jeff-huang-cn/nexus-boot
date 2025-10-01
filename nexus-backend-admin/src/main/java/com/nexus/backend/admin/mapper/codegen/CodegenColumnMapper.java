package com.nexus.backend.admin.mapper.codegen;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexus.backend.admin.entity.codegen.CodegenColumn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 代码生成表字段定义 Mapper 接口
 *
 * @author yourcompany
 * @since 2024-01-01
 */
@Mapper
public interface CodegenColumnMapper extends BaseMapper<CodegenColumn> {

    /**
     * 根据表ID查询字段列表
     *
     * @param tableId 表ID
     * @return 字段列表
     */
    List<CodegenColumn> selectByTableId(@Param("tableId") Long tableId);

    /**
     * 根据表ID查询主键字段
     *
     * @param tableId 表ID
     * @return 主键字段
     */
    CodegenColumn selectPrimaryKeyByTableId(@Param("tableId") Long tableId);

    /**
     * 根据表ID和字段名查询字段信息
     *
     * @param tableId 表ID
     * @param columnName 字段名
     * @return 字段信息
     */
    CodegenColumn selectByTableIdAndColumnName(@Param("tableId") Long tableId, @Param("columnName") String columnName);

    /**
     * 根据表ID查询用于列表显示的字段
     *
     * @param tableId 表ID
     * @return 字段列表
     */
    List<CodegenColumn> selectListResultColumnsByTableId(@Param("tableId") Long tableId);

    /**
     * 根据表ID查询用于查询条件的字段
     *
     * @param tableId 表ID
     * @return 字段列表
     */
    List<CodegenColumn> selectListOperationColumnsByTableId(@Param("tableId") Long tableId);

}
