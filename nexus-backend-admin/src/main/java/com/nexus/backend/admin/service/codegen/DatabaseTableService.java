package com.nexus.backend.admin.service.codegen;

import com.nexus.backend.admin.controller.codegen.vo.DatabaseColumnVO;
import com.nexus.backend.admin.controller.codegen.vo.DatabaseTableDVO;

import java.util.List;

/**
 * 数据库表查询服务
 *
 * @author yourcompany
 * @since 2024-01-01
 */
public interface DatabaseTableService {

    /**
     * 查询数据库表列表
     *
     * @param datasourceConfigId 数据源配置ID
     * @param tableName 表名（可选，用于模糊查询）
     * @return 表列表
     */
    List<DatabaseTableDVO> selectTableList(Long datasourceConfigId, String tableName);

    /**
     * 查询表字段列表
     *
     * @param datasourceConfigId 数据源配置ID
     * @param tableName 表名
     * @return 字段列表
     */
    List<DatabaseColumnVO> selectColumnList(Long datasourceConfigId, String tableName);

    /**
     * 查询表信息
     *
     * @param datasourceConfigId 数据源配置ID
     * @param tableName 表名
     * @return 表信息
     */
    DatabaseTableDVO selectTableByName(Long datasourceConfigId, String tableName);

}
