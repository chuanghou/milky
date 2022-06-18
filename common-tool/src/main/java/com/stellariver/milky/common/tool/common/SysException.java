package com.stellariver.milky.common.tool.common;

import com.stellariver.milky.common.base.Error;
import com.stellariver.milky.common.base.ErrorEnum;
import com.stellariver.milky.common.tool.util.Json;

import java.util.*;
import java.util.function.Supplier;

public class SysException extends BaseException {

    public SysException(String message) {
        super(ErrorEnum.SYSTEM_EXCEPTION.message(message));
    }

    public SysException(Object object) {
        super(ErrorEnum.SYSTEM_EXCEPTION.message(Json.toJson(object)));
    }

    public SysException(Error error) {
        super(error);
    }

    public SysException(Error error, Throwable t) {
        super(error, t);
    }

    public SysException(List<Error> errors, Throwable t) {
        super(errors, t);
    }

    static public void anyNullThrow(Object... params) {
        boolean containNullValue = Arrays.stream(params).anyMatch(Objects::isNull);
        if (containNullValue) {
            throw new SysException(ErrorEnumBase.PARAM_IS_NULL);
        }
    }

    static public void nullThrow(Object param) {
        if (param == null) {
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
            throw new SysException(ErrorEnumBase.PARAM_IS_NULL.message(messageObject));
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
            throw new SysException(ErrorEnumBase.UNDEFINED.message(object));
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
