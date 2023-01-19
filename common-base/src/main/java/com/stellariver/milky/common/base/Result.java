package com.stellariver.milky.common.base;


import lombok.NonNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author houchuang
 */
public class Result<T> implements Serializable {

    protected Boolean success = true;

    protected T data;

    protected String code;

    protected String message;

    protected ExceptionType exceptionType;

    protected Map<String, Object> extendInfo;

    protected List<ErrorEnum> errorEnums;

    public Boolean isSuccess() {
        return success;
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

    public List<ErrorEnum> getErrors() {
        return this.errorEnums;
    }

    public ExceptionType getExceptionType() {
        return exceptionType;
    }

    @Override
    public String toString() {
        return "Result{" +
                "success=" + success +
                ", data=" + data +
                ", code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", exceptionType=" + exceptionType +
                ", extendInfo=" + extendInfo +
                ", errorEnums=" + errorEnums +
                '}';
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
        Result<T> result = new Result<>();
        result.success = false;
        result.code = errorEnum.getCode();
        result.message = errorEnum.getMessage();
        result.errorEnums = Collections.singletonList(errorEnum);
        result.exceptionType = exceptionType;
        return result;
    }

    public static <T> Result<T> error(List<ErrorEnum> errorEnums, @NonNull ExceptionType exceptionType) {
        Result<T> result = new Result<>();
        result.success = false;
        result.code = errorEnums.get(0).getCode();
        result.message = errorEnums.get(0).getMessage();
        result.errorEnums = errorEnums;
        result.exceptionType = exceptionType;
        return result;
    }

    public Result<T> extendInfo(Map<String, Object> extendInfo) {
        this.extendInfo = extendInfo;
        return this;
    }

}