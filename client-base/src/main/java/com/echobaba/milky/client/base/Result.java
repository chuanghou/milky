package com.echobaba.milky.client.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {
    /**
     * 请求结果码
     */
    protected Boolean success = true;

    /**
     * 请求结果数据体
     */
    protected T data;

    /**
     * 普通请求只包含一个errorCode
     */
    protected String errorCode;

    /**
     * 普通请求只包含一个errorCodeMessage
     */
    protected String errorMessage;


    /**
     * 异常对象
     */
    protected Throwable throwable;

    /**
     * 单一请求需要返回多个errorCode
     */
    protected List<ErrorCode> errorCodes;

    public static <T> Result<T> success() {
        return new Result<>();
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setData(data);
        return result;
    }

    public static <T> Result<T> with(ErrorCode errorCode) {
        return Result.<T>builder().errorCode(errorCode.getCode())
                .errorMessage(errorCode.getMessage())
                .errorCodes(Collections.singletonList(errorCode))
                .success(false)
                .build();
    }

    public static <T> Result<T> with(ErrorCode errorCode, Throwable throwable) {
        return Result.<T>builder().errorCode(errorCode.getCode())
                .errorMessage(errorCode.getMessage())
                .throwable(throwable)
                .errorCodes(Collections.singletonList(errorCode))
                .success(false)
                .build();
    }

    public static <T> Result<T> with(List<ErrorCode> errorCodes) {
        ErrorCode errorCode = errorCodes.get(0);
        return Result.<T>builder().errorCode(errorCode.getCode())
                .errorMessage(errorCode.getMessage())
                .errorCodes(Collections.singletonList(errorCode))
                .success(false)
                .build();
    }

}