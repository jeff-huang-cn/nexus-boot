package com.nexus.framework.mybatis.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础VO类
 *
 * @author nexus
 * @since 2024-01-01
 */
@Data
public class BaseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String creator;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private String updater;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
