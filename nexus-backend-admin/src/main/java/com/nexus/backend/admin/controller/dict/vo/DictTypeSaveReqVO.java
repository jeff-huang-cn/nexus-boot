package com.nexus.backend.admin.controller.dict.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 字典类型保存 Request VO（新增/修改）
 *
 * @author system
 * @since 2025-10-27
 */
@Data
public class DictTypeSaveReqVO {

    /**
     * 字典类型ID
     */
    private Long id;

    /**
     * 字典类型（唯一标识）
     */
    @NotBlank(message = "字典类型不能为空")
    private String dictType;

    /**
     * 字典名称
     */
    @NotBlank(message = "字典名称不能为空")
    private String dictName;

    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}