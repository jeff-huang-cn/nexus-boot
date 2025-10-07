package com.nexus.backend.admin.controller.codegen.vo;

import com.nexus.framework.mybatis.entity.BasePageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据源配置分页查询VO
 *
 * @author nexus
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DataSourcePageReqVO extends BasePageQuery {

    private String name;
}
