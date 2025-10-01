package com.nexus.backend.admin.controller.codegen.dto;

import lombok.Data;

/**
 * 数据库表信息DTO
 *
 * @author yourcompany
 * @since 2024-01-01
 */
@Data
public class DatabaseTableDTO {

    /**
     * 表名
     */
    private String tableName;

    /**
     * 表注释
     */
    private String tableComment;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;

    /**
     * 表引擎
     */
    private String engine;

    /**
     * 字符集
     */
    private String tableCollation;

    /**
     * 表大小
     */
    private Long dataLength;

    /**
     * 行数
     */
    private Long tableRows;

}
