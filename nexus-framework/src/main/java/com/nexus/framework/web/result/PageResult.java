package com.nexus.framework.web.result;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页返回结果
 *
 * @author nexus
 * @since 2024-01-01
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<T> list;
    private Long total;
    private Long current;
    private Long size;
    private Long pages;
    private Boolean hasNext;
    private Boolean hasPrevious;

    public PageResult() {
    }

    public PageResult(List<T> list, Long total, Long current, Long size) {
        this.list = list;
        this.total = total;
        this.current = current;
        this.size = size;
        this.pages = size > 0 ? (total + size - 1) / size : 0;
        this.hasNext = current < pages;
        this.hasPrevious = current > 1;
    }

    public static <T> PageResult<T> empty() {
        return new PageResult<>();
    }

    public static <T> PageResult<T> of(List<T> list, Long total, Long current, Long size) {
        return new PageResult<>(list, total, current, size);
    }
}
