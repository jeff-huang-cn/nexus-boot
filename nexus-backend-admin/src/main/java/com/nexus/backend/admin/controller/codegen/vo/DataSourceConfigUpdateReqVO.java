package com.nexus.backend.admin.controller.codegen.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 数据源配置更新请求VO
 *
 * @author nexus
 */
@Data
public class DataSourceConfigUpdateReqVO {

    @NotBlank(message = "数据源名称不能为空")
    private String name;

    @NotBlank(message = "数据源连接URL不能为空")
    private String url;

    @NotBlank(message = "用户名不能为空")
    private String username;

    private String password;
}
