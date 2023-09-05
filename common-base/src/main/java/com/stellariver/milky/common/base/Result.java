package com.stellariver.milky.common.base;

import lombok.Data;
import lombok.NonNull;
import lombok.Setter;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author houchuang
 *
 */
@Data
public class Result<T> implements Serializable {


    protected Boolean success = true;

    protected T data;
    protected String code;
    /**
     * the message that can be shown to customer
     */
    protected String message;
    /**
     * the detail message may come from exception
     */
    protected String detailMessage;
    /**
     * exception type, {@link ExceptionType} may business exception or system exception
     */
    protected ExceptionType exceptionType;
    /**
     * on some circumstance, you may need a set of error info
     */
    protected List<ErrorEnum> errorEnums;

    protected String traceId;

    protected Long date = new Date().getTime();

    public Result() {
        TraceIdGetterHolder.setTraceId(this);
    }

    public static <T> Result<T> success() {
        return new Result<>();
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.data = data;
        return result;
    }

    public static <T> Result<T> error(@NonNull ErrorEnum errorEnum, @NonNull ExceptionType exceptionType) {
        return error(Collections.singletonList(errorEnum), exceptionType);
    }

    public static <T> Result<T> error(List<ErrorEnum> errorEnums, @NonNull ExceptionType exceptionType) {
        Result<T> result = new Result<>();
        result.success = false;
        result.code = errorEnums.get(0).getCode();
        result.detailMessage = errorEnums.get(0).getMessage();
        result.errorEnums = errorEnums;
        result.exceptionType = exceptionType;
        result.message = exceptionType == ExceptionType.BIZ ? result.detailMessage : "系统繁忙，请稍后再试！";
        return result;
    }

    public static void initTraceIdGetter(TraceIdGetter traceIdGetter) {
        TraceIdGetterHolder.setTraceIdGetter(traceIdGetter);
    }

    private static class TraceIdGetterHolder{

        @Setter
        static private TraceIdGetter traceIdGetter;

        public static void setTraceId(Result<?> result) {
            if (traceIdGetter != null) {
                result.setTraceId(traceIdGetter.getTraceId());
            }
        }

    }
}