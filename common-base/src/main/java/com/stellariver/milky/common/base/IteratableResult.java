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
public class IteratableResult<T> extends Result<List<T>> {

    private String nextPageKey;

    private IteratableResult() {
        super();
    }

    private IteratableResult(List<T> data, String nextPageKey) {
        this.success = true;
        this.data = data;
        this.nextPageKey = nextPageKey;
    }

    public static <T> IteratableResult<T> success(List<T> data, String pageKey) {
        return new IteratableResult<>(data, pageKey);
    }

    public static <T> IteratableResult<T> empty() {
        return success(Collections.emptyList(), null);
    }

    public static <T> IteratableResult<T> pageError(ErrorEnum errorEnum, ExceptionType type) {
        return pageError(Collections.singletonList(errorEnum), type);
    }

    public static <T> IteratableResult<T> pageError(List<ErrorEnum> errorEnums, ExceptionType exceptionType) {
        IteratableResult<T> result = new IteratableResult<>();
        result.success = false;
        result.code = errorEnums.get(0).getCode();
        result.detailMessage = errorEnums.get(0).getMessage();
        result.errorEnums = errorEnums;
        result.exceptionType = exceptionType;
        result.message = exceptionType == ExceptionType.BIZ ? result.detailMessage : "系统繁忙，请稍后再试！";
        return result;
    }

}
