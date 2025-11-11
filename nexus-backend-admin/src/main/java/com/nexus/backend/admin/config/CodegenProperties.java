package com.nexus.backend.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 代码生成器配置属性
 *
 * @author yourcompany
 * @since 2024-01-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "nexus.codegen")
public class CodegenProperties {

    /**
     * 排除的表名前缀列表
     * 这些前缀的表不会出现在导入数据库表的列表中
     */
    private List<String> excludeTablePrefixes = new ArrayList<>();

    /**
     * 默认作者名称
     */
    private String defaultAuthor = "backend";

    /**
     * 默认模板类型：1-单表CRUD 2-树表 3-主子表
     */
    private Integer defaultTemplateType = 1;

    /**
     * 默认前端类型：30-React
     */
    private Integer defaultFrontType = 30;

    /**
     * 默认场景：1-管理后台
     */
    private Integer defaultScene = 1;
}