package com.stellariver.milky.common.base;

import java.util.Collections;
import java.util.List;

public class PageResult<T> extends Result<List<T>> {

    private long pageNo;

    private long pageSize;

    private long total;

    private PageResult() {
        super();
    }

    private PageResult(List<T> data, long total, long pageNo, long pageSize) {
        super();
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

    public static <T> PageResult<T> success(List<T> data, long total, long pageNo, long pageSize) {
        return new PageResult<>(data, total, pageNo, pageSize);
    }

    public static <T> PageResult<T> empty(long pageNo, long pageSize) {
        return success(Collections.emptyList(), 0L, pageNo, pageSize);
    }

    public static <T> PageResult<T> error(Error error) {
        PageResult<T> pageResult = new PageResult<>();
        pageResult.success = false;
        pageResult.errorCode =error.getCode();
        pageResult.message = error.getMessage();
        pageResult.errors = Collections.singletonList(error);
        return pageResult;
    }

    public static <T> PageResult<T> errors(List<Error> errors) {
        PageResult<T> pageResult = new PageResult<>();
        pageResult.success = false;
        pageResult.errorCode = errors.get(0).getCode();
        pageResult.message = errors.get(0).getMessage();
        pageResult.errors = errors;
        return pageResult;
    }

}
