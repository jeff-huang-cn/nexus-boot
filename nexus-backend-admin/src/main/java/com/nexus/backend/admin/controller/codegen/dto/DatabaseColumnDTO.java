package com.nexus.backend.admin.controller.codegen.dto;

import lombok.Data;

/**
 * 数据库字段信息DTO
 *
 * @author yourcompany
 * @since 2024-01-01
 */
@Data
public class DatabaseColumnDTO {

    /**
     * 字段名
     */
    private String columnName;

    /**
     * 字段类型
     */
    private String dataType;

    /**
     * 字段注释
     */
    private String columnComment;

    /**
     * 是否允许为空
     */
    private String isNullable;

    /**
     * 字段键信息
     */
    private String columnKey;

    /**
     * 额外信息（如auto_increment）
     */
    private String extra;

    /**
     * 默认值
     */
    private String columnDefault;

    /**
     * 字段位置
     */
    private Integer ordinalPosition;

    /**
     * 字符最大长度
     */
    private Long characterMaximumLength;

    /**
     * 数值精度
     */
    private Integer numericPrecision;

    /**
     * 数值刻度
     */
    private Integer numericScale;

}
