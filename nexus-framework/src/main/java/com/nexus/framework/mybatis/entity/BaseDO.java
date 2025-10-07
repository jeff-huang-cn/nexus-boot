package com.nexus.framework.mybatis.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = false)
public class BaseDO extends BaseUpdateDO {
    @Serial
    private static final long serialVersionUID = 3183031918473848578L;

    @TableLogic
    @TableField(value = "deleted")
    private Boolean deleted;
}
