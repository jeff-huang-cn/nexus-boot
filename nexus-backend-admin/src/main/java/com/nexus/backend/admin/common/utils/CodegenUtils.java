package com.nexus.backend.admin.common.utils;

import com.nexus.backend.admin.controller.codegen.vo.DatabaseColumnVO;
import com.nexus.backend.admin.controller.codegen.vo.DatabaseTableDVO;
import com.nexus.backend.admin.dal.entity.codegen.CodegenColumnDO;
import com.nexus.backend.admin.dal.entity.codegen.CodegenTableDO;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 代码生成工具类
 *
 * @author yourcompany
 * @since 2024-01-01
 */
public class CodegenUtils {

    /**
     * 表名转换成Java类名
     *
     * @param tableName   表名
     * @param classPrefix 类名前缀
     * @return Java类名
     */
    public static String tableToJavaName(String tableName, String classPrefix) {
        if (!StringUtils.hasText(tableName)) {
            return "";
        }

        // 移除表前缀
        String name = tableName;
        if (StringUtils.hasText(classPrefix)) {
            name = removePrefix(name, classPrefix.toLowerCase() + "_");
        }

        // 转换为驼峰命名
        return toCamelCase(name, true);
    }

    /**
     * 列名转换成Java属性名
     *
     * @param columnName 列名
     * @return Java属性名
     */
    public static String columnToJavaName(String columnName) {
        return toCamelCase(columnName, false);
    }

    /**
     * 转换为驼峰命名
     *
     * @param name           名称
     * @param firstUpperCase 首字母是否大写
     * @return 驼峰命名
     */
    public static String toCamelCase(String name, boolean firstUpperCase) {
        if (!StringUtils.hasText(name)) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        String[] words = name.toLowerCase().split("_");

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (word.length() > 0) {
                if (i == 0 && !firstUpperCase) {
                    result.append(word);
                } else {
                    result.append(Character.toUpperCase(word.charAt(0)));
                    if (word.length() > 1) {
                        result.append(word.substring(1));
                    }
                }
            }
        }

        return result.toString();
    }

    /**
     * 移除前缀
     */
    private static String removePrefix(String str, String prefix) {
        if (str != null && str.toLowerCase().startsWith(prefix)) {
            return str.substring(prefix.length());
        }
        return str;
    }

    /**
     * 简单前缀处理：提取第一个下划线前的部分作为前缀建议
     * 
     * @param tableName 表名
     * @return 建议的前缀
     */
    public static String suggestTablePrefix(String tableName) {
        if (!StringUtils.hasText(tableName)) {
            return "";
        }

        int underscoreIndex = tableName.indexOf("_");
        if (underscoreIndex > 0) {
            return tableName.substring(0, underscoreIndex + 1); // 包含下划线
        }

        return ""; // 没有下划线，无前缀
    }

    /**
     * 移除表名前缀
     * 
     * @param tableName 表名
     * @param prefix    前缀（可以为空或null）
     * @return 移除前缀后的表名
     */
    public static String removeTablePrefix(String tableName, String prefix) {
        if (!StringUtils.hasText(tableName)) {
            return tableName;
        }

        if (!StringUtils.hasText(prefix)) {
            return tableName; // 无前缀，直接返回
        }

        if (tableName.toLowerCase().startsWith(prefix.toLowerCase())) {
            return tableName.substring(prefix.length());
        }

        return tableName;
    }

    /**
     * 智能推导业务名：自动检测并移除前缀，转驼峰
     * 
     * @param tableName 表名
     * @return 业务名
     */
    public static String getBusinessName(String tableName) {
        if (!StringUtils.hasText(tableName)) {
            return "";
        }

        // 自动检测并移除前缀
        String prefix = suggestTablePrefix(tableName);
        String cleanName = removeTablePrefix(tableName, prefix);

        return toCamelCase(cleanName, false);
    }

    /**
     * 智能推导类名：业务名首字母大写
     *
     * @param businessName 业务名
     * @return 类名
     */
    public static String getClassName(String businessName) {
        if (!StringUtils.hasText(businessName)) {
            return "";
        }
        return businessName.substring(0, 1).toUpperCase() + businessName.substring(1);
    }

    /**
     * 智能推导模块名：从业务名提取主要部分
     * 参考yudao项目模块划分规则
     *
     * @param businessName 业务名
     * @return 模块名
     */
    public static String getModuleName(String businessName) {
        if (!StringUtils.hasText(businessName)) {
            return "system";
        }

        String lowerBusinessName = businessName.toLowerCase();

        // 如果业务名包含常见的模块词汇，提取前面部分作为模块名
        String[] modulePatterns = { "config", "info", "data", "record", "log", "detail" };
        for (String pattern : modulePatterns) {
            if (lowerBusinessName.contains(pattern)) {
                return lowerBusinessName.replace(pattern, "");
            }
        }

        // 默认返回业务名本身作为模块名
        return lowerBusinessName;
    }

    /**
     * 数据库类型转换为Java类型
     *
     * @param dataType 数据库类型
     * @return Java类型
     */
    public static String convertDataType(String dataType) {
        if (!StringUtils.hasText(dataType)) {
            return "String";
        }

        String type = dataType.toLowerCase();

        if (type.contains("char") || type.contains("text")) {
            return "String";
        } else if (type.contains("bigint")) {
            return "Long";
        } else if (type.contains("int")) {
            return "Integer";
        } else if (type.contains("float")) {
            return "Float";
        } else if (type.contains("double")) {
            return "Double";
        } else if (type.contains("decimal") || type.contains("numeric")) {
            return "BigDecimal";
        } else if (type.contains("date") || type.contains("time")) {
            return "LocalDateTime";
        } else if (type.contains("bit") || type.contains("boolean")) {
            return "Boolean";
        } else if (type.contains("blob") || type.contains("binary")) {
            return "byte[]";
        }

        return "String";
    }

    /**
     * 根据字段类型获取HTML类型
     *
     * @param javaType   Java类型
     * @param columnName 字段名
     * @return HTML类型
     */
    public static String getHtmlType(String javaType, String columnName) {
        String name = columnName.toLowerCase();

        if (name.contains("password")) {
            return "password";
        } else if (name.contains("email")) {
            return "email";
        } else if (name.contains("url")) {
            return "url";
        } else if (name.contains("phone") || name.contains("mobile")) {
            return "tel";
        } else if (name.contains("date") || name.contains("time")) {
            return "datetime";
        } else if (name.contains("status") || name.contains("type") || name.contains("level")) {
            return "select";
        } else if (name.contains("remark") || name.contains("description") || name.contains("content")) {
            return "textarea";
        } else if ("Boolean".equals(javaType)) {
            return "radio";
        } else if ("Integer".equals(javaType) || "Long".equals(javaType) ||
                "Float".equals(javaType) || "Double".equals(javaType) || "BigDecimal".equals(javaType)) {
            return "input";
        }

        return "input";
    }

    /**
     * 初始化代码生成表信息
     *
     * @param table              数据库表信息
     * @param datasourceConfigId 数据源配置ID
     * @return 代码生成表
     */
    public static CodegenTableDO initTable(DatabaseTableDVO table, Long datasourceConfigId) {
        CodegenTableDO codegenTableDO = new CodegenTableDO();

        // 基本信息
        codegenTableDO.setTableName(table.getTableName());

        // 智能设置表描述：如果数据库没有注释，根据类名生成
        String tableName = table.getTableName();
        String businessName = getBusinessName(tableName);
        String className = getClassName(businessName);
        codegenTableDO.setTableComment(
                StringUtils.hasText(table.getTableComment())
                        ? table.getTableComment()
                        : className + "信息表");
        codegenTableDO.setDatasourceConfigId(datasourceConfigId);

        // 智能推导模块名：从业务名提取主要部分
        String moduleName = getModuleName(businessName);

        // 设置生成信息
        codegenTableDO.setClassName(className);
        codegenTableDO.setBusinessName(businessName);
        codegenTableDO.setModuleName(moduleName);
        codegenTableDO.setClassComment(
                StringUtils.hasText(table.getTableComment())
                        ? table.getTableComment()
                        : className + "实体");

        // 设置包名信息（按yudao规范）
        codegenTableDO.setPackageName("com.beckend.admin.modules." + moduleName);
        codegenTableDO.setModulePackageName("com.beckend.admin.modules." + moduleName);
        codegenTableDO.setBusinessPackageName("com.beckend.admin.modules." + moduleName + "." + businessName);

        // 默认配置
        codegenTableDO.setAuthor("beckend");
        codegenTableDO.setTemplateType(1); // CRUD
        codegenTableDO.setFrontType(30); // React
        codegenTableDO.setScene(1); // 管理后台
        codegenTableDO.setClassPrefix("");

        // 审计字段
        codegenTableDO.setCreator("system");
        codegenTableDO.setDateCreated(LocalDateTime.now());
        codegenTableDO.setUpdater("system");
        codegenTableDO.setLastUpdated(LocalDateTime.now());

        return codegenTableDO;
    }

    /**
     * 初始化代码生成字段信息
     *
     * @param column  数据库字段信息
     * @param tableId 表ID
     * @return 代码生成字段
     */
    public static CodegenColumnDO initColumn(DatabaseColumnVO column, Long tableId) {
        CodegenColumnDO codegenColumnDO = new CodegenColumnDO();

        // 基本信息
        codegenColumnDO.setTableId(tableId);
        codegenColumnDO.setColumnName(column.getColumnName());
        codegenColumnDO.setDataType(column.getDataType());
        codegenColumnDO.setColumnComment(
                StringUtils.hasText(column.getColumnComment()) ? column.getColumnComment() : column.getColumnName());

        // 字段属性
        codegenColumnDO.setNullable(!"NO".equals(column.getIsNullable()));
        codegenColumnDO.setPrimaryKey("PRI".equals(column.getColumnKey()));
        codegenColumnDO.setAutoIncrement("auto_increment".equals(column.getExtra()) ? "1" : "0");
        codegenColumnDO.setOrdinalPosition(column.getOrdinalPosition());

        // Java属性
        codegenColumnDO.setJavaType(convertDataType(column.getDataType()));
        codegenColumnDO.setJavaField(columnToJavaName(column.getColumnName()));

        // 表单属性
        codegenColumnDO.setHtmlType(getHtmlType(codegenColumnDO.getJavaType(), column.getColumnName()));

        // 操作属性
        String columnName = column.getColumnName().toLowerCase();
        if (codegenColumnDO.getPrimaryKey() || "date_created".equals(columnName) ||
                "last_updated".equals(columnName) || "deleted".equals(columnName) ||
                "creator".equals(columnName) || "updater".equals(columnName) || "tenant_id".equals(columnName)) {
            // 主键和审计字段默认不参与新增和修改
            codegenColumnDO.setCreateOperation(false);
            codegenColumnDO.setUpdateOperation(false);
        } else {
            codegenColumnDO.setCreateOperation(true);
            codegenColumnDO.setUpdateOperation(true);
        }

        if ("password".equals(columnName) || "deleted".equals(columnName)) {
            // 密码和删除标志不显示在列表中
            codegenColumnDO.setListOperation(false);
            codegenColumnDO.setListOperationResult(false);
        } else {
            codegenColumnDO.setListOperation(true);
            codegenColumnDO.setListOperationResult(true);
        }

        // 查询条件
        if ("String".equals(codegenColumnDO.getJavaType())) {
            codegenColumnDO.setListOperationCondition("LIKE");
        } else if ("LocalDateTime".equals(codegenColumnDO.getJavaType()) ||
                "LocalDate".equals(codegenColumnDO.getJavaType()) ||
                "Date".equals(codegenColumnDO.getJavaType())) {
            codegenColumnDO.setListOperationCondition("BETWEEN");
        } else {
            codegenColumnDO.setListOperationCondition("EQ");
        }

        // 示例数据
        codegenColumnDO.setExample(getExampleValue(codegenColumnDO.getJavaType(), column.getColumnName()));

        // 审计字段
        codegenColumnDO.setCreator("system");
        codegenColumnDO.setDateCreated(LocalDateTime.now());
        codegenColumnDO.setUpdater("system");
        codegenColumnDO.setLastUpdated(LocalDateTime.now());

        return codegenColumnDO;
    }

    /**
     * 获取示例值
     */
    private static String getExampleValue(String javaType, String columnName) {
        String name = columnName.toLowerCase();

        if (name.contains("name")) {
            return "示例名称";
        } else if (name.contains("email")) {
            return "example@email.com";
        } else if (name.contains("phone") || name.contains("mobile")) {
            return "13888888888";
        } else if (name.contains("url")) {
            return "https://example.com";
        } else if (name.contains("address")) {
            return "示例地址";
        }

        switch (javaType) {
            case "String":
                return "示例文本";
            case "Integer":
            case "Long":
                return "1";
            case "BigDecimal":
                return "0.00";
            case "Boolean":
                return "true";
            case "LocalDateTime":
                return "2024-01-01 00:00:00";
            default:
                return "";
        }
    }

}
