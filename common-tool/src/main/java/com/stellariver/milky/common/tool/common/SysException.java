package com.stellariver.milky.common.tool.common;

import com.stellariver.milky.common.base.Code;

import java.util.*;
import java.util.function.Supplier;

public class SysException extends RuntimeException {

    private final String errorCode;

    public SysException(Code code) {
        super(code.getMessage());
        this.errorCode = code.getCode();
    }

    public SysException(String message) {
        super(message);
        this.errorCode = CodeEnumBase.UNDEFINED.getCode();
    }


    public SysException(Code code, Throwable t) {
        super(code.getMessage(), t);
        this.errorCode = code.getCode();
    }

    public SysException(Throwable t) {
        super(t);
        this.errorCode = CodeEnumBase.UNDEFINED.getCode();
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    static public void nullThrow(Object... params) {
        boolean containNullValue = Arrays.stream(params).anyMatch(Objects::isNull);
        if (containNullValue) {
            throw new SysException(CodeEnumBase.PARAM_IS_NULL);
        }
    }

    static public void nullThrow(Object param, Supplier<Code> supplier) {
        if (param == null) {
            throw new SysException(supplier.get());
        }
    }

    static public void trueThrow(boolean test, Supplier<Code> supplier) {
        if (test) {
            throw new SysException(supplier.get());
        }
    }

    static public void falseThrow(boolean test, Supplier<Code> supplier) {
        if (!test) {
            throw new SysException(supplier.get());
        }
    }

    static public void trueThrow(boolean test, String message) {
        if (test) {
            throw new SysException(CodeEnumBase.UNDEFINED.message(message));
        }
    }

    static public void falseThrow(boolean test, String message) {
        if (!test) {
            throw new SysException(CodeEnumBase.UNDEFINED.message(message));
        }
    }
}
