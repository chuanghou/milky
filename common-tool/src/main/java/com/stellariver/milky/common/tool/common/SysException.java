package com.stellariver.milky.common.tool.common;

import com.stellariver.milky.common.base.ErrorCode;

import java.util.*;
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
        boolean containNullValue = Arrays.stream(params).anyMatch(Objects::isNull);
        if (containNullValue) {
            throw new SysException(ErrorCodeEnumBase.PARAM_IS_NULL);
        }
    }

    static public void nullThrow(Object param, Supplier<ErrorCode> supplier) {
        if (param == null) {
            throw new SysException(supplier.get());
        }
    }

    static public void trueThrow(boolean test, Supplier<ErrorCode> supplier) {
        if (test) {
            throw new SysException(supplier.get());
        }
    }


    static public void falseThrow(boolean test, Supplier<ErrorCode> supplier) {
        if (!test) {
            throw new SysException(supplier.get());
        }
    }

    static public void trueThrow(boolean test, String message) {
        if (test) {
            throw new SysException(ErrorCodeEnumBase.UNDEFINED.message(message));
        }
    }

    static public void falseThrow(boolean test, String message) {
        if (!test) {
            throw new SysException(ErrorCodeEnumBase.UNDEFINED.message(message));
        }
    }
}
