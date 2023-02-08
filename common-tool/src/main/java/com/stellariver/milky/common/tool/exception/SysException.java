package com.stellariver.milky.common.tool.exception;

import com.stellariver.milky.common.base.ErrorEnum;

import java.util.*;
import java.util.function.Supplier;

/**
 * @author houchuang
 */
public class SysException extends BaseException {

    public SysException(Object object) {
        super(Collections.singletonList(ErrorEnumsBase.SYSTEM_EXCEPTION.message(object)));
    }

    public SysException(Throwable throwable) {
        super(throwable);
    }

    public SysException(ErrorEnum errorEnum) {
        super(Collections.singletonList(errorEnum));
    }

    public SysException(ErrorEnum errorEnum, Throwable t) {
        super(Collections.singletonList(errorEnum), t);
    }

    public SysException(List<ErrorEnum> errorEnums, Throwable t) {
        super(errorEnums, t);
    }

    static public void anyNullThrow(Object... params) {
        boolean containNullValue = Arrays.stream(params).anyMatch(Objects::isNull);
        if (containNullValue) {
            throw new SysException(ErrorEnumsBase.PARAM_IS_NULL);
        }
    }

    static public void nullThrow(Object param) {
        if (param == null) {
            throw new SysException(ErrorEnumsBase.PARAM_IS_NULL);
        }
    }

    static public void nullThrow(Object param, Object message) {
        if (param == null) {
            throw new SysException(ErrorEnumsBase.PARAM_IS_NULL.message(message));
        }
    }

    static public void trueThrowGet(boolean test, Supplier<ErrorEnum> supplier) {
        if (test) {
            throw new SysException(supplier.get());
        }
    }

    static public void trueThrowGet(boolean test, Supplier<ErrorEnum> supplier, boolean fillStackTrace) {
        if (test) {
            throw new SysException(supplier.get());
        }
    }

    static public void trueThrow(boolean test, ErrorEnum errorEnum) {
        if (test) {
            throw new SysException(errorEnum);
        }
    }

    static public void trueThrow(boolean test, Object object) {
        if (test) {
            throw new SysException(ErrorEnumsBase.SYSTEM_EXCEPTION.message(object));
        }
    }

    static public void falseThrow(boolean test, ErrorEnum errorEnum) {
        trueThrow(!test, errorEnum);
    }

    static public void falseThrowGet(boolean test, Supplier<ErrorEnum> supplier) {
        trueThrowGet(!test, supplier);
    }

    static public void falseThrow(boolean test,  Object object) {
        trueThrow(!test, object);
    }

    static public SysException unreachable() {
        return new SysException(ErrorEnumsBase.UNREACHABLE_CODE);
    }

}
