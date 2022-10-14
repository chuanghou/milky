package com.stellariver.milky.common.base;


import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class Result<T> implements Serializable {

    protected Boolean success = true;

    protected T data;

    protected String code;

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

    public String getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

    public List<Error> getErrors() {
        return this.errors;
    }

    @Override
    public String toString() {
        return "Result{" +
                "success=" + success +
                ", data=" + data +
                ", code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", errors=" + errors +
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

    public static <T> Result<T> error(Error error) {
        Result<T> result = new Result<>();
        result.success = false;
        result.code = error.getCode();
        result.message = error.getMessage();
        result.errors = Collections.singletonList(error);
        return result;
    }

    public static <T> Result<T> error(List<Error> errors) {
        Result<T> result = new Result<>();
        result.success = false;
        result.code = errors.get(0).getCode();
        result.message = errors.get(0).getMessage();
        result.errors = errors;
        return result;
    }

}