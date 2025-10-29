package com.nexus.backend.admin.controller.dict.vo;

import lombok.Data;

/**
 * 数据字典分页查询 Request VO
 *
 * @author system
 * @since 2025-10-27
 */
@Data
public class DictPageReqVO {

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;

    /**
     * 字典类型（精确查询）
     */
    private String dictType;

    /**
     * 字典标签（模糊查询）
     */
    private String dictLabel;

    /**
     * 字典键值（模糊查询）
     */
    private String dictValue;

    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;
}