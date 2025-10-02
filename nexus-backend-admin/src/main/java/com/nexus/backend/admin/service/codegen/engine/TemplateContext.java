package com.nexus.backend.admin.service.codegen.engine;

import com.nexus.backend.admin.dal.entity.codegen.CodegenColumnDO;
import com.nexus.backend.admin.dal.entity.codegen.CodegenTableDO;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模板上下文构建器
 *
 * @author yourcompany
 * @since 2024-01-01
 */
@Data
public class TemplateContext {

    /**
     * 构建模板变量
     *
     * @param table   表配置
     * @param columns 字段配置
     * @return 模板变量Map
     */
    public static Map<String, Object> build(CodegenTableDO table, List<CodegenColumnDO> columns) {
        Map<String, Object> context = new HashMap<>();

        // 基本信息
        context.put("table", table);
        context.put("columns", columns);
        context.put("tableName", table.getTableName());
        context.put("tableComment", table.getTableComment());
        context.put("className", table.getClassName());
        context.put("classComment", table.getClassComment());
        context.put("businessName", table.getBusinessName());
        context.put("moduleName", table.getModuleName());
        context.put("packageName", table.getPackageName());
        context.put("modulePackageName", table.getModulePackageName());
        context.put("businessPackageName", table.getBusinessPackageName());
        context.put("author", table.getAuthor());
        context.put("datetime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        context.put("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        // 字段信息
        context.put("primaryKey", getPrimaryKeyColumn(columns));
        context.put("createColumns", getCreateColumns(columns));
        context.put("updateColumns", getUpdateColumns(columns));
        context.put("listColumns", getListColumns(columns));
        context.put("listResultColumns", getListResultColumns(columns));

        // Java相关
        context.put("classNameLower", table.getClassName().toLowerCase());
        context.put("classNameFirstLower", getFirstLowerCase(table.getClassName()));
        context.put("businessNameUpper", getFirstUpperCase(table.getBusinessName()));

        // 导入包信息
        context.put("hasDate", hasDateColumn(columns));
        context.put("hasBigDecimal", hasBigDecimalColumn(columns));
        context.put("hasLocalDateTime", hasLocalDateTimeColumn(columns));

        // 前端相关
        context.put("frontType", table.getFrontType());
        context.put("isReact", table.getFrontType() == 30);

        // BaseDO 字段列表（用于DO模板排除这些字段）
        context.put("baseDOFields", getBaseDOFields());

        return context;
    }

    /**
     * 获取 BaseDO 包含的字段名列表
     */
    private static List<String> getBaseDOFields() {
        return List.of("dateCreated", "lastUpdated", "creator", "updater", "deleted", "tenantId");
    }

    /**
     * 获取主键字段
     */
    private static CodegenColumnDO getPrimaryKeyColumn(List<CodegenColumnDO> columns) {
        return columns.stream()
                .filter(CodegenColumnDO::getPrimaryKey)
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取新增操作字段
     */
    private static List<CodegenColumnDO> getCreateColumns(List<CodegenColumnDO> columns) {
        return columns.stream()
                .filter(CodegenColumnDO::getCreateOperation)
                .collect(Collectors.toList());
    }

    /**
     * 获取编辑操作字段
     */
    private static List<CodegenColumnDO> getUpdateColumns(List<CodegenColumnDO> columns) {
        return columns.stream()
                .filter(CodegenColumnDO::getUpdateOperation)
                .collect(Collectors.toList());
    }

    /**
     * 获取查询操作字段
     */
    private static List<CodegenColumnDO> getListColumns(List<CodegenColumnDO> columns) {
        return columns.stream()
                .filter(CodegenColumnDO::getListOperation)
                .collect(Collectors.toList());
    }

    /**
     * 获取列表显示字段
     */
    private static List<CodegenColumnDO> getListResultColumns(List<CodegenColumnDO> columns) {
        return columns.stream()
                .filter(CodegenColumnDO::getListOperationResult)
                .collect(Collectors.toList());
    }

    /**
     * 首字母小写
     */
    private static String getFirstLowerCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * 首字母大写
     */
    private static String getFirstUpperCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * 是否包含日期字段
     */
    private static boolean hasDateColumn(List<CodegenColumnDO> columns) {
        return columns.stream()
                .anyMatch(column -> "Date".equals(column.getJavaType()) ||
                        "LocalDate".equals(column.getJavaType()) ||
                        "LocalDateTime".equals(column.getJavaType()));
    }

    /**
     * 是否包含BigDecimal字段
     */
    private static boolean hasBigDecimalColumn(List<CodegenColumnDO> columns) {
        return columns.stream()
                .anyMatch(column -> "BigDecimal".equals(column.getJavaType()));
    }

    /**
     * 是否包含LocalDateTime字段
     */
    private static boolean hasLocalDateTimeColumn(List<CodegenColumnDO> columns) {
        return columns.stream()
                .anyMatch(column -> "LocalDateTime".equals(column.getJavaType()));
    }

}
