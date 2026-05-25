package com.onlinetrain.common;

import lombok.Data;

import java.util.List;

/**
 * 分页结果包装类
 */
@Data
public class PageResult<T> {

    /** 总记录数 */
    private Long total;

    /** 总页数 */
    private Long pages;

    /** 当前页 */
    private Long current;

    /** 每页大小 */
    private Long size;

    /** 数据列表 */
    private List<T> records;

    public PageResult() {}

    public PageResult(Long total, Long pages, Long current, Long size, List<T> records) {
        this.total = total;
        this.pages = pages;
        this.current = current;
        this.size = size;
        this.records = records;
    }

    /**
     * 从 MyBatis-Plus Page 构建分页结果
     */
    public static <T> PageResult<T> of(com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> page) {
        return new PageResult<>(
                page.getTotal(),
                page.getPages(),
                page.getCurrent(),
                page.getSize(),
                page.getRecords()
        );
    }

    /**
     * 手动构建分页结果
     */
    public static <T> PageResult<T> of(List<T> records, Long total, Long current, Long size) {
        long pages = size > 0 ? (total + size - 1) / size : 0;
        return new PageResult<>(total, pages, current, size, records);
    }
}
