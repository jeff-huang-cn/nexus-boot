package com.nexus.framework.mybatis.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体对象 - 包含公共字段
 * 
 * @author nexus
 */
@Data
public class BaseDO implements Serializable {

    /**
     * 创建时间
     */
    @TableField(value = "date_created", fill = FieldFill.INSERT)
    private LocalDateTime dateCreated;

    /**
     * 更新时间
     */
    @TableField(value = "last_updated", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime lastUpdated;

    /**
     * 创建者，目前使用 SysUser 的 id 编号
     */
    @TableField(fill = FieldFill.INSERT)
    private String creator;

    /**
     * 更新者，目前使用 SysUser 的 id 编号
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updater;

    /**
     * 是否删除（逻辑删除）
     */
    @TableLogic
    private Boolean deleted;

    /**
     * 租户编号
     */
    private Long tenantId;
}
