package com.nexus.backend.admin.dal.entity.codegen;

import com.baomidou.mybatisplus.annotation.*;
import com.nexus.framework.mybatis.entity.BaseUpdateEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 代码生成表定义
 *
 * @author yourcompany
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("codegen_table")
public class CodegenTableDO extends BaseUpdateEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 表名称
     */
    @TableField("table_name")
    private String tableName;

    /**
     * 表描述
     */
    @TableField("table_comment")
    private String tableComment;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 模块名
     */
    @TableField("module_name")
    private String moduleName;

    /**
     * 业务名
     */
    @TableField("business_name")
    private String businessName;

    /**
     * 类名称
     */
    @TableField("class_name")
    private String className;

    /**
     * 类描述
     */
    @TableField("class_comment")
    private String classComment;

    /**
     * 作者
     */
    @TableField("author")
    private String author;

    /**
     * 模板类型：1-CRUD 2-树表 3-主子表
     */
    @TableField("template_type")
    private Integer templateType;

    /**
     * 前端类型：10-Vue2 20-Vue3 21-Vue3 Schema 30-React
     */
    @TableField("front_type")
    private Integer frontType;

    /**
     * 场景：1-管理后台 2-用户APP
     */
    @TableField("scene")
    private Integer scene;

    /**
     * 生成包路径
     */
    @TableField("package_name")
    private String packageName;

    /**
     * 生成模块包路径
     */
    @TableField("module_package_name")
    private String modulePackageName;

    /**
     * 生成业务包路径
     */
    @TableField("business_package_name")
    private String businessPackageName;

    /**
     * 类名称前缀
     */
    @TableField("class_prefix")
    private String classPrefix;

    /**
     * 主表编号（主子表专用）
     */
    @TableField("master_table_id")
    private Long masterTableId;

    /**
     * 子表关联主表的字段名（主子表专用）
     */
    @TableField("sub_join_column_name")
    private String subJoinColumnName;

    /**
     * 主表与子表是否一对多（主子表专用）
     */
    @TableField("sub_join_many")
    private Boolean subJoinMany;

    /**
     * 树表的父字段名（树表专用）
     */
    @TableField("tree_parent_column_name")
    private String treeParentColumnName;

    /**
     * 树表的名字段名（树表专用）
     */
    @TableField("tree_name_column_name")
    private String treeNameColumnName;

    /**
     * 数据源配置编号
     */
    @TableField("datasource_config_id")
    private Long datasourceConfigId;
}
