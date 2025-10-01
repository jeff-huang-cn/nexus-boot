package com.nexus.backend.admin.mapper.codegen;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexus.backend.admin.entity.codegen.CodegenTable;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 代码生成表定义 Mapper 接口
 *
 * @author yourcompany
 * @since 2024-01-01
 */
@Mapper
public interface CodegenTableMapper extends BaseMapper<CodegenTable> {

    /**
     * 根据数据源ID查询表列表
     *
     * @param datasourceConfigId 数据源配置ID
     * @return 表列表
     */
    List<CodegenTable> selectByDatasourceConfigId(@Param("datasourceConfigId") Long datasourceConfigId);

    /**
     * 根据表名查询表信息
     *
     * @param tableName 表名
     * @param datasourceConfigId 数据源配置ID
     * @return 表信息
     */
    CodegenTable selectByTableName(@Param("tableName") String tableName, @Param("datasourceConfigId") Long datasourceConfigId);

    /**
     * 根据模块名查询表列表
     *
     * @param moduleName 模块名
     * @return 表列表
     */
    List<CodegenTable> selectByModuleName(@Param("moduleName") String moduleName);

}
