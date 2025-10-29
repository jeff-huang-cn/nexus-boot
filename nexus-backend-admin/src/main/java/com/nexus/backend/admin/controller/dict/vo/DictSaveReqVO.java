package com.nexus.backend.admin.controller.dict.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 数据字典保存 Request VO（新增/修改）
 *
 * @author system
 * @since 2025-10-27
 */
@Data
public class DictSaveReqVO {

    /**
     * 字典ID
     */
    private Long id;

    /**
     * 字典类型（分类标识）
     */
    @NotBlank(message = "字典类型不能为空")
    private String dictType;

    /**
     * 字典标签（显示值）
     */
    @NotBlank(message = "字典标签不能为空")
    private String dictLabel;

    /**
     * 字典键值（存储值）
     */
    @NotBlank(message = "字典键值不能为空")
    private String dictValue;

    /**
     * 排序号
     */
    private Integer dictSort;

    /**
     * CSS样式类
     */
    private String cssClass;

    /**
     * 列表样式（default/primary/success/info/warning/danger）
     */
    private String listClass;

    /**
     * 是否默认：0-否 1-是
     */
    private Integer isDefault;

    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}