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

    public Error getFirstErrorCode() {
        return errors.get(0);
    }

    public List<Error> getErrorCodes() {
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

    static public void trueThrow(boolean test, Supplier<Error> supplier) {
        if (test) {
            throw new BizException(supplier.get());
        }
    }

    static public void emptyThrow(Collection<?> collection, Supplier<Error> supplier) {
        if (Collect.isEmpty(collection)) {
            throw new BizException(supplier.get());
        }
    }

    static public void falseThrow(boolean test, Supplier<Error> supplier) {
        if (!test) {
            throw new BizException(supplier.get());
        }
    }

    static private ThreadLocal<List<Error>>  temporaryErrorCodes;

    /**
     * 对于任何一个init函数都必须在有try-final块里面有 removeTemporaryErrorCodes（）操作
     */
    static public void initTemporaryErrorCodes() {
        temporaryErrorCodes = new ThreadLocal<>();
        temporaryErrorCodes.set(new ArrayList<>());
    }

    static public void removeTemporaryErrorCodes() {
        Optional.ofNullable(temporaryErrorCodes).ifPresent(ThreadLocal::remove);
    }

    static public void addTemporaryErrorCode(Error error) {
        Optional.ofNullable(temporaryErrorCodes)
                .map(ThreadLocal::get)
                .orElseThrow(() -> new BizException(ErrorEnumBase
                        .CONFIG_ERROR.message("temporaryErrorCodes container need explicitly init!")))
                .add(error);
    }

    static public List<Error> getTemporaryErrorCodes() {
        return Optional.ofNullable(temporaryErrorCodes).map(ThreadLocal::get).orElse(new ArrayList<>());
    }

}
