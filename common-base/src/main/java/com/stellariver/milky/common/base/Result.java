package com.stellariver.milky.common.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
    @Builder.Default
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
    protected List<Code> codes;

    public static <T> Result<T> success() {
        return new Result<>();
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setData(data);
        return result;
    }

    public static <T> Result<T> with(Code code) {
        return Result.<T>builder().errorCode(code.getCode())
                .errorMessage(code.getMessage())
                .codes(Collections.singletonList(code))
                .success(false)
                .build();
    }

    public static <T> Result<T> with(Code code, Throwable throwable) {
        return Result.<T>builder().errorCode(code.getCode())
                .errorMessage(code.getMessage())
                .throwable(throwable)
                .codes(Collections.singletonList(code))
                .success(false)
                .build();
    }

    public static <T> Result<T> with(List<Code> codes) {
        Code code = codes.get(0);
        return Result.<T>builder().errorCode(code.getCode())
                .errorMessage(code.getMessage())
                .codes(Collections.singletonList(code))
                .success(false)
                .build();
    }

}