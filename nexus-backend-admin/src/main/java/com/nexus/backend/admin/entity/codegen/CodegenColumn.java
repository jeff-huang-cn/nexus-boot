package com.nexus.backend.admin.entity.codegen;

import com.baomidou.mybatisplus.annotation.*;
import com.nexus.framework.mybatis.entity.BaseEntity;
import com.nexus.framework.mybatis.entity.BaseUpdateEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 代码生成表字段定义
 *
 * @author yourcompany
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("codegen_column")
public class CodegenColumn extends BaseUpdateEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 表编号
     */
    @TableField("table_id")
    private Long tableId;

    /**
     * 字段名
     */
    @TableField("column_name")
    private String columnName;

    /**
     * 字段类型
     */
    @TableField("data_type")
    private String dataType;

    /**
     * 字段描述
     */
    @TableField("column_comment")
    private String columnComment;

    /**
     * 是否允许为空
     */
    @TableField("nullable")
    private Boolean nullable;

    /**
     * 是否主键
     */
    @TableField("primary_key")
    private Boolean primaryKey;

    /**
     * 是否自增
     */
    @TableField("auto_increment")
    private String autoIncrement;

    /**
     * 排序
     */
    @TableField("ordinal_position")
    private Integer ordinalPosition;

    /**
     * Java 属性类型
     */
    @TableField("java_type")
    private String javaType;

    /**
     * Java 属性名
     */
    @TableField("java_field")
    private String javaField;

    /**
     * 字典类型
     */
    @TableField("dict_type")
    private String dictType;

    /**
     * 数据示例
     */
    @TableField("example")
    private String example;

    /**
     * 是否为Create创建操作的字段
     */
    @TableField("create_operation")
    private Boolean createOperation;

    /**
     * 是否为Update更新操作的字段
     */
    @TableField("update_operation")
    private Boolean updateOperation;

    /**
     * 是否为List查询操作的字段
     */
    @TableField("list_operation")
    private Boolean listOperation;

    /**
     * List查询操作的条件类型
     */
    @TableField("list_operation_condition")
    private String listOperationCondition;

    /**
     * 是否为List查询操作的返回字段
     */
    @TableField("list_operation_result")
    private Boolean listOperationResult;

    /**
     * 显示类型
     */
    @TableField("html_type")
    private String htmlType;
}
