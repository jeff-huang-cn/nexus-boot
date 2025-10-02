package com.nexus.backend.admin.dal.mapper.codegen;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexus.backend.admin.dal.dataobject.codegen.CodegenColumnDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 代码生成表字段定义 Mapper 接口
 *
 * @author yourcompany
 * @since 2024-01-01
 */
@Mapper
public interface CodegenColumnMapper extends BaseMapper<CodegenColumnDO> {

    /**
     * 根据表ID查询字段列表
     *
     * @param tableId 表ID
     * @return 字段列表
     */
    default List<CodegenColumnDO> selectByTableId(Long tableId) {
        return selectList(new LambdaQueryWrapper<CodegenColumnDO>()
                .eq(CodegenColumnDO::getTableId, tableId)
                .orderByAsc(CodegenColumnDO::getOrdinalPosition));
    }

    /**
     * 根据表ID查询主键字段
     *
     * @param tableId 表ID
     * @return 主键字段
     */
    default CodegenColumnDO selectPrimaryKeyByTableId(Long tableId) {
        return selectOne(new LambdaQueryWrapper<CodegenColumnDO>()
                .eq(CodegenColumnDO::getTableId, tableId)
                .eq(CodegenColumnDO::getPrimaryKey, 1)
                .last("LIMIT 1"));
    }

    /**
     * 根据表ID和字段名查询字段信息
     *
     * @param tableId 表ID
     * @param columnName 字段名
     * @return 字段信息
     */
    default CodegenColumnDO selectByTableIdAndColumnName(Long tableId, String columnName) {
        return selectOne(new LambdaQueryWrapper<CodegenColumnDO>()
                .eq(CodegenColumnDO::getTableId, tableId)
                .eq(CodegenColumnDO::getColumnName, columnName)
                .last("LIMIT 1"));
    }

    /**
     * 根据表ID查询用于列表显示的字段
     *
     * @param tableId 表ID
     * @return 字段列表
     */
    default List<CodegenColumnDO> selectListResultColumnsByTableId(Long tableId) {
        return selectList(new LambdaQueryWrapper<CodegenColumnDO>()
                .eq(CodegenColumnDO::getTableId, tableId)
                .eq(CodegenColumnDO::getListOperationResult, 1)
                .orderByAsc(CodegenColumnDO::getOrdinalPosition));
    }

    /**
     * 根据表ID查询用于查询条件的字段
     *
     * @param tableId 表ID
     * @return 字段列表
     */
    default List<CodegenColumnDO> selectListOperationColumnsByTableId(Long tableId) {
        return selectList(new LambdaQueryWrapper<CodegenColumnDO>()
                .eq(CodegenColumnDO::getTableId, tableId)
                .eq(CodegenColumnDO::getListOperation, 1)
                .orderByAsc(CodegenColumnDO::getOrdinalPosition));
    }

}
