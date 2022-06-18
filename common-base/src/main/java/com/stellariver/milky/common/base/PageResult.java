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
        paginator.setPage(pageNo);
        paginator.setItemsPerPage(pageSize);
        paginator.setPages(20);
        paginator.setItems(total);
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


    public static <T> PageResult<T> errorPageResult(Error error) {
        PageResult<T> result = new PageResult<>();
        result.success = false;
        result.errorCode = error.getCode();
        result.message = error.getMessage();
        result.errors = Collections.singletonList(error);
        return result;
    }

}
