package com.nexus.backend.admin.controller.codegen.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据源配置响应VO
 *
 * @author nexus
 */
@Data
public class DataSourceConfigRespVO {

    private Long id;

    private String name;

    private String url;

    private String username;

    /**
     * 密码（列表查询时脱敏，详情查询时返回真实值）
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String password;

    /**
     * 密码是否已配置
     */
    private Boolean hasPassword;

    private LocalDateTime dateCreated;

    private LocalDateTime lastUpdated;
}
