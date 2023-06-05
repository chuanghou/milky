package com.stellariver.milky.common.base;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collections;
import java.util.List;

/**
 * @author houchuang
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PageResult<T> extends Result<List<T>> {

    private String nextPageKey;

    @NotNull
    @PositiveOrZero
    private Long total;

    private PageResult() {
        super();
    }

    private PageResult(List<T> data, long total, String nextPageKey) {
        this.success = true;
        this.data = data;
        this.total = total;
        this.nextPageKey = nextPageKey;
    }

    public static <T> PageResult<T> success(List<T> data, long total,String pageKey) {
        return new PageResult<>(data, total, pageKey);
    }

    public static <T> PageResult<T> empty() {
        return success(Collections.emptyList(), 0L, null);
    }

    public static <T> PageResult<T> pageError(ErrorEnum errorEnum, ExceptionType type) {
        return pageError(Collections.singletonList(errorEnum), type);
    }

    public static <T> PageResult<T> pageError(List<ErrorEnum> errorEnums, ExceptionType exceptionType) {
        PageResult<T> result = new PageResult<>();
        result.success = false;
        result.code = errorEnums.get(0).getCode();
        result.detailMessage = errorEnums.get(0).getMessage();
        result.errorEnums = errorEnums;
        result.exceptionType = exceptionType;
        result.message = exceptionType == ExceptionType.BIZ ? result.detailMessage : "系统繁忙，请稍后再试！";
        return result;
    }

}
