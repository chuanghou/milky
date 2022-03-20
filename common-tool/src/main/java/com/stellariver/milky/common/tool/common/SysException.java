package com.stellariver.milky.common.tool.common;

import com.stellariver.milky.common.base.ErrorCode;
import com.stellariver.milky.common.tool.util.Collect;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 */
public class SysException extends RuntimeException {

    private final String errorCode;

    public SysException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode.getCode();
    }
    public SysException(ErrorCode errorCode, Throwable t) {
        super(errorCode.getMessage(), t);
        this.errorCode = errorCode.getCode();
    }

    public String getErrorCode() {
        return this.errorCode;
    }



    static public void nullThrow(Object... params) {
        boolean haveNullValue = Arrays.stream(params).anyMatch(Objects::isNull);
        if (haveNullValue) {
            throw new SysException(ErrorCodeEnumBase.PARAM_IS_NULL);
        }
    }

    static public <T, K> void nullThrow(T param, Function<T, K> deepFinder) {
        if (param == null) {
            throw new SysException(ErrorCodeEnumBase.PARAM_IS_NULL);
        }
        if (deepFinder.apply(param) == null) {
            throw new SysException(ErrorCodeEnumBase.PARAM_IS_NULL);
        }
    }

    static public void nullThrow(Object param, Supplier<ErrorCode> supplier) {
        if (param == null) {
            throw new SysException(supplier.get());
        }
    }

    static public void nullThrow(Object param, ErrorCode errorCode) {
        if (param == null) {
            throw new SysException(errorCode);
        }
    }

    static public void with(ErrorCode errorCode) {
        throw new SysException(errorCode);
    }

    static public void trueThrow(boolean test, Supplier<ErrorCode> supplier) {
        if (test) {
            throw new SysException(supplier.get());
        }
    }

    static public void emptyThrow(Collection<?> collection, Supplier<ErrorCode> supplier) {
        if (Collect.isEmpty(collection)) {
            throw new SysException(supplier.get());
        }
    }

    static public void falseThrow(boolean test, Supplier<ErrorCode> supplier) {
        if (!test) {
            throw new SysException(supplier.get());
        }
    }
}
