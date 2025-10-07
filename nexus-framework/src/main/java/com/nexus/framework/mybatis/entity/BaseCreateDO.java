package com.nexus.framework.mybatis.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class BaseCreateDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 5395666692573184417L;

    @TableField(value = "creator", fill = FieldFill.INSERT)
    private String creator;

    @TableField(value = "date_created", fill = FieldFill.INSERT)
    private LocalDateTime dateCreated;
}
