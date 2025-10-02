package com.nexus.framework.mybatis.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.io.Serial;
import java.io.Serializable;

/**
 * 分页查询基类
 *
 * @author nexus
 * @since 2024-01-01
 */
@Data
public class BasePageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = -229795724510992449L;

    @Min(value = 1, message = "页码必须大于0")
    private Long current = 1L;

    @Min(value = 1, message = "每页条数必须大于0")
    @Max(value = 999, message = "每页条数不能超过999")
    private Long size = 10L;

    private String orderBy;

    private String orderDirection = "ASC";

    @JsonIgnore
    public Long getOffset() {
        return (current - 1) * size;
    }

    @JsonIgnore
    public boolean needSort() {
        return orderBy != null && !orderBy.trim().isEmpty();
    }
}
