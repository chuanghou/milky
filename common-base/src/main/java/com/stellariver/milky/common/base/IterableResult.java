package com.stellariver.milky.common.base;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collections;
import java.util.List;

/**
 * @author houchuang
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class IterableResult<T> extends Result<List<T>> {

    private String nextPageKey;

    private IterableResult() {
        super();
    }

    private IterableResult(List<T> data, String nextPageKey) {
        this.success = true;
        this.data = data;
        this.nextPageKey = nextPageKey;
    }

    public static <T> IterableResult<T> success(List<T> data, String pageKey) {
        return new IterableResult<>(data, pageKey);
    }

    public static <T> IterableResult<T> empty() {
        return success(Collections.emptyList(), null);
    }

    public static <T> IterableResult<T> pageError(ErrorEnum errorEnum, ExceptionType type) {
        return pageError(Collections.singletonList(errorEnum), type);
    }

    public static <T> IterableResult<T> pageError(List<ErrorEnum> errorEnums, ExceptionType exceptionType) {
        IterableResult<T> result = new IterableResult<>();
        result.success = false;
        result.code = errorEnums.get(0).getCode();
        result.detailMessage = errorEnums.get(0).getMessage();
        result.errorEnums = errorEnums;
        result.exceptionType = exceptionType;
        result.message = exceptionType == ExceptionType.BIZ ? result.detailMessage : "系统繁忙，请稍后再试！";
        return result;
    }

}
