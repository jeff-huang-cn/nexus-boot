package com.nexus.backend.admin.controller.codegen;

import com.nexus.backend.admin.controller.codegen.vo.DatabaseColumnVO;
import com.nexus.backend.admin.controller.codegen.vo.DatabaseTableDVO;
import com.nexus.backend.admin.dal.dataobject.codegen.DataSourceConfigDO;
import com.nexus.backend.admin.dal.mapper.codegen.DataSourceConfigMapper;
import com.nexus.backend.admin.service.codegen.DatabaseTableService;
import com.nexus.framework.web.result.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 数据库管理控制器
 *
 * @author yourcompany
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/database")
@Validated
public class DatabaseController {

    @Resource
    private DatabaseTableService databaseTableService;

    @Resource
    private DataSourceConfigMapper dataSourceConfigMapper;

    /**
     * 查询数据源配置列表
     *
     * @return 数据源配置列表
     */
    @GetMapping("/datasources")
    public Result<List<DataSourceConfigDO>> getDataSourceList() {
        List<DataSourceConfigDO> list = dataSourceConfigMapper.selectActiveList();
        return Result.success(list);
    }

    /**
     * 查询数据库表列表
     *
     * @param datasourceConfigId 数据源配置ID
     * @param tableName          表名（可选，用于模糊查询）
     * @return 表列表
     */
    @GetMapping("/tables")
    public Result<List<DatabaseTableDVO>> getTableList(
            @RequestParam @NotNull Long datasourceConfigId,
            @RequestParam(required = false) String tableName) {

        List<DatabaseTableDVO> list = databaseTableService.selectTableList(datasourceConfigId, tableName);
        return Result.success(list);
    }

    /**
     * 查询表字段列表
     *
     * @param datasourceConfigId 数据源配置ID
     * @param tableName          表名
     * @return 字段列表
     */
    @GetMapping("/tables/{tableName}/columns")
    public Result<List<DatabaseColumnVO>> getColumnList(
            @RequestParam @NotNull Long datasourceConfigId,
            @PathVariable @NotBlank String tableName) {

        List<DatabaseColumnVO> list = databaseTableService.selectColumnList(datasourceConfigId, tableName);
        return Result.success(list);
    }

    /**
     * 查询表信息
     *
     * @param datasourceConfigId 数据源配置ID
     * @param tableName          表名
     * @return 表信息
     */
    @GetMapping("/tables/{tableName}")
    public Result<DatabaseTableDVO> getTableInfo(
            @RequestParam @NotNull Long datasourceConfigId,
            @PathVariable @NotBlank String tableName) {

        DatabaseTableDVO table = databaseTableService.selectTableByName(datasourceConfigId, tableName);
        return Result.success(table);
    }

}
