package com.stellariver.milky.common.base;

import java.util.Collections;
import java.util.List;

public class PageResult<T> extends Result<List<T>> {

    private final Paginator paginator = new Paginator();

    private PageResult() {
        super();
    }

    private PageResult(List<T> data, long total, long pageNo, long pageSize) {
        super();
        this.success = true;
        this.data = data;
        paginator.setPageNo(pageNo);
        paginator.setPageSize(pageSize);
        paginator.setPageCount((total/pageSize + (total%pageSize == 0 ? 0 : 1)));
        paginator.setTotal(total);
    }

    public Paginator getPaginator() {
        return this.paginator;
    }

    public static <T> PageResult<T> success(List<T> data, long total, long pageNo, long pageSize) {
        return new PageResult<>(data, total, pageNo, pageSize);
    }

    public static <T> PageResult<T> empty(long pageNo, long pageSize) {
        return success(Collections.emptyList(), 0L, pageNo, pageSize);
    }

    public static <T> PageResult<T> pageError(Error error) {
        return pageError(Collections.singletonList(error));
    }

    public static <T> PageResult<T> pageError(List<Error> errors) {
        PageResult<T> result = new PageResult<>();
        result.success = false;
        result.code = errors.get(0).getCode();
        result.message = errors.get(0).getMessage();
        result.errors = errors;
        return result;
    }

}
