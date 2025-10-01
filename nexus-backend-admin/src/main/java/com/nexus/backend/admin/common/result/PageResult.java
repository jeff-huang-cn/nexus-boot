package com.nexus.backend.admin.common.result;

import lombok.Data;

import java.util.List;

/**
 * 分页返回结果
 *
 * @author yourcompany
 * @since 2024-01-01
 */
@Data
public class PageResult<T> {

    /**
     * 数据列表
     */
    private List<T> list;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Long current;

    /**
     * 每页条数
     */
    private Long size;

    /**
     * 总页数
     */
    private Long pages;

    public PageResult() {}

    public PageResult(List<T> list, Long total, Long current, Long size) {
        this.list = list;
        this.total = total;
        this.current = current;
        this.size = size;
        this.pages = size > 0 ? (total + size - 1) / size : 0;
    }

    /**
     * 创建空的分页结果
     *
     * @param <T> 数据类型
     * @return 分页结果
     */
    public static <T> PageResult<T> empty() {
        return new PageResult<>();
    }

    /**
     * 创建分页结果
     *
     * @param list 数据列表
     * @param total 总记录数
     * @param current 当前页码
     * @param size 每页条数
     * @param <T> 数据类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(List<T> list, Long total, Long current, Long size) {
        return new PageResult<>(list, total, current, size);
    }

}
