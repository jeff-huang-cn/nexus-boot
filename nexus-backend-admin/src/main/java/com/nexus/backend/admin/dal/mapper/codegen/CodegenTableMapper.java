package com.nexus.backend.admin.dal.mapper.codegen;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexus.backend.admin.dal.dataobject.codegen.CodegenTableDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 代码生成表定义 Mapper 接口
 *
 * @author yourcompany
 * @since 2024-01-01
 */
@Mapper
public interface CodegenTableMapper extends BaseMapper<CodegenTableDO> {

    /**
     * 根据数据源ID查询表列表
     *
     * @param datasourceConfigId 数据源配置ID
     * @return 表列表
     */
    default List<CodegenTableDO> selectByDatasourceConfigId(Long datasourceConfigId) {
        return selectList(new LambdaQueryWrapper<CodegenTableDO>()
                .eq(CodegenTableDO::getDatasourceConfigId, datasourceConfigId)
                .orderByDesc(CodegenTableDO::getDateCreated));
    }

    /**
     * 根据表名查询表信息
     *
     * @param tableName 表名
     * @param datasourceConfigId 数据源配置ID
     * @return 表信息
     */
    default CodegenTableDO selectByTableName(String tableName, Long datasourceConfigId) {
        return selectOne(new LambdaQueryWrapper<CodegenTableDO>()
                .eq(CodegenTableDO::getTableName, tableName)
                .eq(CodegenTableDO::getDatasourceConfigId, datasourceConfigId)
                .last("LIMIT 1"));
    }

    /**
     * 根据模块名查询表列表
     *
     * @param moduleName 模块名
     * @return 表列表
     */
    default List<CodegenTableDO> selectByModuleName(String moduleName) {
        return selectList(new LambdaQueryWrapper<CodegenTableDO>()
                .eq(CodegenTableDO::getModuleName, moduleName)
                .orderByDesc(CodegenTableDO::getDateCreated));
    }

}
