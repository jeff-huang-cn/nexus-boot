package com.nexus.framework.mybatis.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class BaseUpdateDO extends BaseCreateDO {

    @Serial
    private static final long serialVersionUID = 7127871884214865060L;

    @TableField(value = "updater", fill = FieldFill.INSERT_UPDATE)
    private String updater;

    @TableField(value = "last_updated", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime lastUpdated;
}
