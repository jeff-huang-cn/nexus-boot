package com.nexus.framework.mybatis.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
public class BaseEntity extends BaseUpdateEntity {
    @Serial
    private static final long serialVersionUID = 3183031918473848578L;

    @TableLogic
    @TableField(value = "deleted")
    private Boolean deleted;
}
