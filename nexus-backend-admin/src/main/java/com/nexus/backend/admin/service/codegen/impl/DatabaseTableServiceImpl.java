package com.nexus.backend.admin.service.codegen.impl;

import com.nexus.backend.admin.common.exception.BusinessException;
import com.nexus.backend.admin.controller.codegen.vo.DatabaseColumnVO;
import com.nexus.backend.admin.controller.codegen.vo.DatabaseTableDVO;
import com.nexus.backend.admin.dal.dataobject.codegen.DataSourceConfigDO;
import com.nexus.backend.admin.dal.mapper.codegen.DataSourceConfigMapper;
import com.nexus.backend.admin.service.codegen.DatabaseTableService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库表查询服务实现
 *
 * @author yourcompany
 * @since 2024-01-01
 */
@Slf4j
@Service
public class DatabaseTableServiceImpl implements DatabaseTableService {

    @Resource
    private DataSourceConfigMapper dataSourceConfigMapper;

    @Override
    public List<DatabaseTableDVO> selectTableList(Long datasourceConfigId, String tableName) {
        DataSourceConfigDO config = getDataSourceConfig(datasourceConfigId);

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("  table_name, ");
        sql.append("  table_comment, ");
        sql.append("  create_time, ");
        sql.append("  update_time, ");
        sql.append("  engine, ");
        sql.append("  table_collation, ");
        sql.append("  data_length, ");
        sql.append("  table_rows ");
        sql.append("FROM information_schema.tables ");
        sql.append("WHERE table_schema = DATABASE() ");
        sql.append("  AND table_type = 'BASE TABLE' ");

        if (StringUtils.hasText(tableName)) {
            sql.append("  AND table_name LIKE ? ");
        }

        sql.append("ORDER BY table_name");

        List<DatabaseTableDVO> list = new ArrayList<>();
        try (Connection conn = createConnection(config);
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (StringUtils.hasText(tableName)) {
                stmt.setString(paramIndex++, "%" + tableName + "%");
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DatabaseTableDVO dto = new DatabaseTableDVO();
                    dto.setTableName(rs.getString("table_name"));
                    dto.setTableComment(rs.getString("table_comment"));
                    dto.setCreateTime(rs.getString("create_time"));
                    dto.setUpdateTime(rs.getString("update_time"));
                    dto.setEngine(rs.getString("engine"));
                    dto.setTableCollation(rs.getString("table_collation"));
                    dto.setDataLength(rs.getLong("data_length"));
                    dto.setTableRows(rs.getLong("table_rows"));
                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            log.error("查询数据库表列表失败", e);
            throw new BusinessException("查询数据库表列表失败：" + e.getMessage());
        }

        return list;
    }

    @Override
    public List<DatabaseColumnVO> selectColumnList(Long datasourceConfigId, String tableName) {
        DataSourceConfigDO config = getDataSourceConfig(datasourceConfigId);

        String sql = "SELECT " +
                "  column_name, " +
                "  data_type, " +
                "  column_comment, " +
                "  is_nullable, " +
                "  column_key, " +
                "  extra, " +
                "  column_default, " +
                "  ordinal_position, " +
                "  character_maximum_length, " +
                "  numeric_precision, " +
                "  numeric_scale " +
                "FROM information_schema.columns " +
                "WHERE table_schema = DATABASE() " +
                "  AND table_name = ? " +
                "ORDER BY ordinal_position";

        List<DatabaseColumnVO> list = new ArrayList<>();
        try (Connection conn = createConnection(config);
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tableName);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DatabaseColumnVO dto = new DatabaseColumnVO();
                    dto.setColumnName(rs.getString("column_name"));
                    dto.setDataType(rs.getString("data_type"));
                    dto.setColumnComment(rs.getString("column_comment"));
                    dto.setIsNullable(rs.getString("is_nullable"));
                    dto.setColumnKey(rs.getString("column_key"));
                    dto.setExtra(rs.getString("extra"));
                    dto.setColumnDefault(rs.getString("column_default"));
                    dto.setOrdinalPosition(rs.getInt("ordinal_position"));
                    dto.setCharacterMaximumLength(rs.getLong("character_maximum_length"));
                    dto.setNumericPrecision(rs.getInt("numeric_precision"));
                    dto.setNumericScale(rs.getInt("numeric_scale"));
                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            log.error("查询表字段列表失败", e);
            throw new BusinessException("查询表字段列表失败：" + e.getMessage());
        }

        return list;
    }

    @Override
    public DatabaseTableDVO selectTableByName(Long datasourceConfigId, String tableName) {
        List<DatabaseTableDVO> list = selectTableList(datasourceConfigId, tableName);
        return list.stream()
                .filter(table -> tableName.equals(table.getTableName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取数据源配置
     */
    private DataSourceConfigDO getDataSourceConfig(Long datasourceConfigId) {
        DataSourceConfigDO config = dataSourceConfigMapper.selectById(datasourceConfigId);
        if (config == null) {
            throw new BusinessException("数据源配置不存在");
        }
        return config;
    }

    /**
     * 创建数据库连接
     */
    private Connection createConnection(DataSourceConfigDO config) throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(config.getUrl(), config.getUsername(), config.getPassword());
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL驱动未找到", e);
        }
    }

}
