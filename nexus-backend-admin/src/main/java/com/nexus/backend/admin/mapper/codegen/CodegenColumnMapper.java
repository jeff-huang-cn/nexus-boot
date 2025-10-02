package com.nexus.backend.admin.mapper.codegen;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexus.backend.admin.entity.codegen.CodegenColumn;
import org.apache.ibatis.annotations.Mapper;

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
    default List<CodegenColumn> selectByTableId(Long tableId) {
        return selectList(new LambdaQueryWrapper<CodegenColumn>()
                .eq(CodegenColumn::getTableId, tableId)
                .orderByAsc(CodegenColumn::getOrdinalPosition));
    }

    /**
     * 根据表ID查询主键字段
     *
     * @param tableId 表ID
     * @return 主键字段
     */
    default CodegenColumn selectPrimaryKeyByTableId(Long tableId) {
        return selectOne(new LambdaQueryWrapper<CodegenColumn>()
                .eq(CodegenColumn::getTableId, tableId)
                .eq(CodegenColumn::getPrimaryKey, 1)
                .last("LIMIT 1"));
    }

    /**
     * 根据表ID和字段名查询字段信息
     *
     * @param tableId 表ID
     * @param columnName 字段名
     * @return 字段信息
     */
    default CodegenColumn selectByTableIdAndColumnName(Long tableId, String columnName) {
        return selectOne(new LambdaQueryWrapper<CodegenColumn>()
                .eq(CodegenColumn::getTableId, tableId)
                .eq(CodegenColumn::getColumnName, columnName)
                .last("LIMIT 1"));
    }

    /**
     * 根据表ID查询用于列表显示的字段
     *
     * @param tableId 表ID
     * @return 字段列表
     */
    default List<CodegenColumn> selectListResultColumnsByTableId(Long tableId) {
        return selectList(new LambdaQueryWrapper<CodegenColumn>()
                .eq(CodegenColumn::getTableId, tableId)
                .eq(CodegenColumn::getListOperationResult, 1)
                .orderByAsc(CodegenColumn::getOrdinalPosition));
    }

    /**
     * 根据表ID查询用于查询条件的字段
     *
     * @param tableId 表ID
     * @return 字段列表
     */
    default List<CodegenColumn> selectListOperationColumnsByTableId(Long tableId) {
        return selectList(new LambdaQueryWrapper<CodegenColumn>()
                .eq(CodegenColumn::getTableId, tableId)
                .eq(CodegenColumn::getListOperation, 1)
                .orderByAsc(CodegenColumn::getOrdinalPosition));
    }

}
