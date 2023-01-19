package com.stellariver.milky.common.base;

import java.util.Collections;
import java.util.List;

/**
 * @author houchuang
 */
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

    public static <T> PageResult<T> empty(long pageSize) {
        return success(Collections.emptyList(), 0L, 1, pageSize);
    }

    public static <T> PageResult<T> pageError(ErrorEnum errorEnum, ExceptionType type) {
        return pageError(Collections.singletonList(errorEnum), type);
    }

    public static <T> PageResult<T> pageError(List<ErrorEnum> errorEnums, ExceptionType exceptionType) {
        PageResult<T> result = new PageResult<>();
        result.success = false;
        result.code = errorEnums.get(0).getCode();
        result.message = errorEnums.get(0).getMessage();
        result.errorEnums = errorEnums;
        result.exceptionType = exceptionType;
        return result;
    }

}
