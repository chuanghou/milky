package com.echobaba.milky.client.base;

import java.util.Collections;
import java.util.List;

public class PageResult<T> extends Result<List<T>> {

    private final long pageNo;

    private final long pageSize;

    private final long total;

    private PageResult(List<T> data, long total, long pageNo, long pageSize) {
        this.success = true;
        this.data = data;
        this.total = total;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

    public long getPageNo() {
        return pageNo;
    }

    public long getPageSize() {
        return pageSize;
    }

    public long getTotal() {
        return total;
    }

    public static <T> PageResult<T> success(List<T> t, long total, long pageNo, long pageSize) {
        return new PageResult<>(t, total, pageNo, pageSize);
    }

    public static <T> PageResult<T> empty(long pageNo, long pageSize) {
        return success(Collections.emptyList(), 0L, pageNo, pageSize);
    }

    public static <T> PageResult<T> empty(int pageNo, int pageSize) {
        return success(Collections.emptyList(), 0L, pageNo, pageSize);
    }

}
