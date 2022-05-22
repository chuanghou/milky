package com.stellariver.milky.common.base;


import java.io.Serializable;
import java.util.Collections;
import java.util.List;


public class Result<T> implements Serializable {

    protected Boolean success = true;

    protected T data;

    protected String errorCode;

    protected String message;

    protected List<Error> errors;

    public Boolean isSuccess() {
        return success;
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public String getMessage() {
        return this.message;
    }

    public List<Error> getErrors() {
        return this.errors;
    }

    public static <T> Result<T> success() {
        return new Result<>();
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.data = data;
        return result;
    }

    public static <T> Result<T> with(Error error) {
        Result<T> result = new Result<>();
        result.success = false;
        result.errorCode = error.getCode();
        result.message = error.getMessage();
        result.errors = Collections.singletonList(error);
        return result;
    }

    public static <T> Result<T> with(List<Error> errors) {
        Result<T> result = new Result<>();
        result.success = false;
        result.errorCode = errors.get(0).getCode();
        result.message = errors.get(0).getMessage();
        result.errors = errors;
        return result;
    }

}