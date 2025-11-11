package com.nexus.backend.admin.controller.dict.vo;

import lombok.Data;

/**
 * 字典项保存 VO
 *
 * @author nexus
 */
@Data
public class DictItemSaveVO {

    /**
     * 字典ID（修改时需要）
     */
    private Long id;

    /**
     * 字典标签
     */
    private String dictLabel;

    /**
     * 字典值
     */
    private String dictValue;

    /**
     * 显示顺序
     */
    private Integer sort;

    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
