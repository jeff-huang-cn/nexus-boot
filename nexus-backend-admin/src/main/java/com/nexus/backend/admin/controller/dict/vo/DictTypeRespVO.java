package com.nexus.backend.admin.controller.dict.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 字典类型响应 VO
 *
 * @author system
 * @since 2025-10-27
 */
@Data
public class DictTypeRespVO {

    /**
     * 字典类型ID
     */
    private Long id;

    /**
     * 字典类型（唯一标识）
     */
    private String dictType;

    /**
     * 字典名称
     */
    private String dictName;

    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 创建者
     */
    private String creator;

    /**
     * 创建时间
     */
    private LocalDateTime dateCreated;

    /**
     * 更新者
     */
    private String updater;

    /**
     * 更新时间
     */
    private LocalDateTime lastUpdated;
}