package com.stellariver.milky.common.tool.exception;

import com.stellariver.milky.common.base.ErrorEnum;

import java.util.*;
import java.util.function.Supplier;

/**
 * @author houchuang
 */
public class BizException extends BaseException {

    public BizException(ErrorEnum errorEnum, Throwable throwable) {
        super(Collections.singletonList(errorEnum), throwable);
    }

    public BizException(ErrorEnum errorEnum, Throwable throwable, boolean fillStackTrace) {
        super(Collections.singletonList(errorEnum), throwable, fillStackTrace);
    }

    public BizException(ErrorEnum errorEnum) {
        super(Collections.singletonList(errorEnum));
    }

    public BizException(ErrorEnum errorEnum, boolean fillStackTrace) {
        super(Collections.singletonList(errorEnum), fillStackTrace);
    }

    public BizException(List<ErrorEnum> errorEnums) {
        super(errorEnums);
    }

    public BizException(List<ErrorEnum> errorEnums, boolean fillStackTrace) {
        super(errorEnums, fillStackTrace);
    }

    static public void anyNullThrow(Object... params) {
        boolean containNullValue = Arrays.stream(params).anyMatch(Objects::isNull);
        if (containNullValue) {
            throw new BizException(ErrorEnumsBase.PARAM_IS_NULL);
        }
    }

    static public void nullThrow(Object param) {
        if (param == null) {
            throw new BizException(ErrorEnumsBase.PARAM_IS_NULL);
        }
    }

    static public void nullThrow(Object param, Object message) {
        if (param == null) {
            throw new BizException(ErrorEnumsBase.PARAM_IS_NULL.message(message));
        }
    }


    static public void trueThrowGet(boolean test, Supplier<ErrorEnum> supplier) {
        if (test) {
            throw new BizException(supplier.get());
        }
    }

    static public void trueThrowGet(boolean test, Supplier<ErrorEnum> supplier, boolean fillStackTrace) {
        if (test) {
            throw new BizException(supplier.get(), fillStackTrace);
        }
    }

    static public void trueThrow(boolean test, ErrorEnum errorEnum) {
        if (test) {
            throw new BizException(errorEnum);
        }
    }

    static public void trueThrow(boolean test, ErrorEnum errorEnum, boolean fillStackTrace) {
        if (test) {
            throw new BizException(errorEnum, fillStackTrace);
        }
    }

    static public void falseThrowGet(boolean test, Supplier<ErrorEnum> supplier) {
        trueThrowGet(!test, supplier);
    }

    static public void falseThrowGet(boolean test, Supplier<ErrorEnum> supplier, boolean fillStackTrace) {
        trueThrowGet(!test, supplier, fillStackTrace);
    }

    static public void falseThrow(boolean test, ErrorEnum errorEnum) {
        trueThrow(!test, errorEnum);
    }

    static public void falseThrow(boolean test, ErrorEnum errorEnum, boolean fillStackTrace) {
        trueThrow(!test, errorEnum, fillStackTrace);
    }

}
