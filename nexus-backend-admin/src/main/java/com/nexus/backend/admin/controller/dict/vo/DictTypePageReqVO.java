package com.nexus.backend.admin.controller.dict.vo;

import lombok.Data;

/**
 * 字典类型分页查询 Request VO
 *
 * @author system
 * @since 2025-10-27
 */
@Data
public class DictTypePageReqVO {

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;

    /**
     * 字典类型（模糊查询）
     */
    private String dictType;

    /**
     * 字典名称（模糊查询）
     */
    private String dictName;

    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;
}