package com.stellariver.milky.common.tool.common;

import com.stellariver.milky.common.base.Error;
import com.stellariver.milky.common.tool.util.Json;

import java.util.*;
import java.util.function.Supplier;

public class SysException extends BaseException {

    private final String errorCode;

    public SysException(Error error) {
        super(error.getMessage());
        this.errorCode = error.getCode();
    }

    public SysException(Object object) {
        super(Objects.toString(object));
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

    static public void anyNullThrow(Object... params) {
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

    static public void nullThrowMessage(Object param, Object messageObject) {
        if (param == null) {
            throw new SysException(Json.toJson(messageObject));
        }
    }

    static public void trueThrowGet(boolean test, Supplier<Error> supplier) {
        if (test) {
            throw new SysException(supplier.get());
        }
    }

    static public void trueThrow(boolean test, Error error) {
        if (test) {
            throw new SysException(error);
        }
    }

    static public void trueThrow(boolean test, Object object) {
        if (test) {
            throw new SysException(Json.toJson(object));
        }
    }


    static public void falseThrowGet(boolean test, Supplier<Error> supplier) {
        trueThrowGet(!test, supplier);
    }

    static public void falseThrow(boolean test, Error error) {
        trueThrow(!test, error);
    }


    static public void falseThrow(boolean test,  Object object) {
        trueThrow(!test, object);
    }
}
