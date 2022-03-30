package com.stellariver.milky.common.tool.common;

import com.stellariver.milky.common.base.Error;
import com.stellariver.milky.common.tool.util.Collect;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 */
public class BizException extends RuntimeException {

    private final String errorCode;

    private final List<Error> errors;

    public BizException(Error error) {
        super(error.getMessage());
        this.errorCode = error.getCode();
        this.errors = Collections.singletonList(error);
    }
    public BizException(Error error, Throwable t) {
        super(error.getMessage(), t);
        this.errorCode = error.getCode();
        this.errors = Collections.singletonList(error);
    }

    public BizException(List<Error> errors, Throwable t) {
        super(errors.get(0).getMessage(), t);
        this.errorCode = errors.get(0).getCode();
        this.errors = errors;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public Error getFirstError() {
        return errors.get(0);
    }

    public List<Error> getErrors() {
        return errors;
    }

    static public void nullThrow(Object... params) {
        boolean haveNullValue = Arrays.stream(params).anyMatch(Objects::isNull);
        if (haveNullValue) {
            throw new BizException(ErrorEnumBase.PARAM_IS_NULL);
        }
    }

    static public <T, K> void nullThrow(T param, Function<T, K> deepFinder) {
        if (param == null) {
            throw new BizException(ErrorEnumBase.PARAM_IS_NULL);
        }
        if (deepFinder.apply(param) == null) {
            throw new BizException(ErrorEnumBase.PARAM_IS_NULL);
        }
    }

    static public void nullThrow(Object param, Supplier<Error> supplier) {
        if (param == null) {
            throw new BizException(supplier.get());
        }
    }

    static public void nullThrow(Object param, Error error) {
        if (param == null) {
            throw new BizException(error);
        }
    }

    static public void with(Error error) {
        throw new BizException(error);
    }

    static public void trueThrow(boolean test, Error error) {
        if (test) {
            throw new BizException(error);
        }
    }

    static public void falseThrow(boolean test, Error error) {
        if (!test) {
            throw new BizException(error);
        }
    }

    static private ThreadLocal<List<Error>> temporaryErrors;

    /**
     * 对于任何一个init函数都必须在有try-final块里面有 removeTemporaryErrorCodes（）操作
     */
    static public void initTemporaryErrors() {
        temporaryErrors = new ThreadLocal<>();
        temporaryErrors.set(new ArrayList<>());
    }

    static public void removeTemporaryErrors() {
        Optional.ofNullable(temporaryErrors).ifPresent(ThreadLocal::remove);
    }

    static public void addTemporaryError(Error error) {
        Optional.ofNullable(temporaryErrors)
                .map(ThreadLocal::get)
                .orElseThrow(() -> new BizException(ErrorEnumBase
                        .CONFIG_ERROR.message("temporaryErrorCodes container need explicitly init!")))
                .add(error);
    }

    static public List<Error> getTemporaryErrors() {
        return Optional.ofNullable(temporaryErrors).map(ThreadLocal::get).orElse(new ArrayList<>());
    }

}
