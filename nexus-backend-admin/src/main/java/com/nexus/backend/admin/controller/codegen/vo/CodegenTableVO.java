package com.nexus.backend.admin.controller.codegen.vo;

import com.nexus.backend.admin.dal.dataobject.codegen.CodegenColumnDO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 代码生成表配置VO
 *
 * @author yourcompany
 * @since 2024-01-01
 */
@Data
public class CodegenTableVO {

    /**
     * 编号
     */
    private Long id;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 表描述
     */
    private String tableComment;

    /**
     * 备注
     */
    private String remark;

    /**
     * 模块名
     */
    private String moduleName;

    /**
     * 业务名
     */
    private String businessName;

    /**
     * 类名称
     */
    private String className;

    /**
     * 类描述
     */
    private String classComment;

    /**
     * 作者
     */
    private String author;

    /**
     * 模板类型：1-CRUD 2-树表 3-主子表
     */
    private Integer templateType;

    /**
     * 前端类型：10-Vue2 20-Vue3 21-Vue3 Schema 30-React
     */
    private Integer frontType;

    /**
     * 场景：1-管理后台 2-用户APP
     */
    private Integer scene;

    /**
     * 生成包路径
     */
    private String packageName;

    /**
     * 生成模块包路径
     */
    private String modulePackageName;

    /**
     * 生成业务包路径
     */
    private String businessPackageName;

    /**
     * 类名称前缀
     */
    private String classPrefix;

    /**
     * 父菜单编号
     */
    private Long parentMenuId;

    /**
     * 数据源配置编号
     */
    private Long datasourceConfigId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 字段列表
     */
    private List<CodegenColumnDO> columns;

}
