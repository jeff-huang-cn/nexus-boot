package com.nexus.backend.admin.controller.dict.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 字典类型批量保存 Request VO
 * 用于批量配置某个字典类型下的所有字典项
 *
 * @author nexus
 */
@Data
public class DictTypeBatchSaveReqVO {

    /**
     * 字典类型（唯一标识）
     */
    @NotBlank(message = "字典类型不能为空")
    private String dictType;

    /**
     * 字典项列表
     */
    @NotNull(message = "字典项列表不能为空")
    private List<DictItemSaveVO> items;
}
