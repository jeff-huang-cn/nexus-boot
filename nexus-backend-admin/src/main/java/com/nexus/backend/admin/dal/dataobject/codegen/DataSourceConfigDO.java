package com.nexus.backend.admin.dal.dataobject.codegen;

import com.baomidou.mybatisplus.annotation.*;
import com.nexus.framework.mybatis.entity.BaseUpdateDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据源配置
 *
 * @author yourcompany
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("datasource_config")
public class DataSourceConfigDO extends BaseUpdateDO {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 数据源名称
     */
    @TableField("name")
    private String name;

    /**
     * 数据源连接
     */
    @TableField("url")
    private String url;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 密码
     */
    @TableField("password")
    private String password;
}
