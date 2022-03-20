package com.stellariver.milky.common.tool.common;

import com.stellariver.milky.common.base.Error;

import java.util.*;
import java.util.function.Supplier;

public class SysException extends RuntimeException {

    private final String errorCode;

    public SysException(Error error) {
        super(error.getMessage());
        this.errorCode = error.getCode();
    }

    public SysException(String message) {
        super(message);
        this.errorCode = ErrorEnumBase.UNDEFINED.getCode();
    }

    public SysException(Error error, Throwable t) {
        super(error.getMessage(), t);
        this.errorCode = error.getCode();
    }

    public SysException(Throwable t) {
        super(t);
        this.errorCode = ErrorEnumBase.UNDEFINED.getCode();
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    static public void nullThrow(Object... params) {
        boolean containNullValue = Arrays.stream(params).anyMatch(Objects::isNull);
        if (containNullValue) {
            throw new SysException(ErrorEnumBase.PARAM_IS_NULL);
        }
    }

    static public void nullThrow(Object param, Supplier<Error> supplier) {
        if (param == null) {
            throw new SysException(supplier.get());
        }
    }

    static public void trueThrow(boolean test, Supplier<Error> supplier) {
        if (test) {
            throw new SysException(supplier.get());
        }
    }

    static public void falseThrow(boolean test, Supplier<Error> supplier) {
        if (!test) {
            throw new SysException(supplier.get());
        }
    }

    static public void trueThrow(boolean test, String message) {
        if (test) {
            throw new SysException(ErrorEnumBase.UNDEFINED.message(message));
        }
    }

    static public void falseThrow(boolean test, String message) {
        if (!test) {
            throw new SysException(ErrorEnumBase.UNDEFINED.message(message));
        }
    }
}
