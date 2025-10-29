package com.nexus.backend.admin.controller.dict.vo;

import lombok.Data;

/**
 * 字典类型分组响应 VO
 * 用于展示字典类型列表（按 dict_type 分组）
 *
 * @author nexus
 */
@Data
public class DictTypeGroupRespVO {

    /**
     * 字典类型（唯一标识）
     */
    private String dictType;

    /**
     * 字典项数量
     */
    private Integer itemCount;

    /**
     * 状态：0-禁用 1-启用（取第一个字典项的状态）
     */
    private Integer status;

    /**
     * 示例标签（显示前3个字典项的标签）
     */
    private String sampleLabels;
}
