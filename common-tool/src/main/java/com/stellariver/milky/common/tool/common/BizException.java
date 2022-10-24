package com.stellariver.milky.common.tool.common;

import com.stellariver.milky.common.base.ErrorEnum;

import java.util.*;
import java.util.function.Supplier;

/**
 */
public class BizException extends BaseException {

    public BizException(ErrorEnum errorEnum, Throwable throwable) {
        super(Collections.singletonList(errorEnum), throwable);
    }

    public BizException(ErrorEnum errorEnum) {
        super(Collections.singletonList(errorEnum));
    }

    public BizException(List<ErrorEnum> errorEnums) {
        super(errorEnums);
    }

    static public void anyNullThrow(Object... params) {
        boolean containNullValue = Arrays.stream(params).anyMatch(Objects::isNull);
        if (containNullValue) {
            throw new BizException(ErrorEnumsBase.PARAM_IS_NULL);
        }
    }

    static public void nullThrow(Object param, Supplier<ErrorEnum> supplier) {
        if (param == null) {
            throw new BizException(supplier.get());
        }
    }

    static public void trueThrowGet(boolean test, Supplier<ErrorEnum> supplier) {
        if (test) {
            throw new BizException(supplier.get());
        }
    }

    static public void trueThrow(boolean test, ErrorEnum errorEnum) {
        if (test) {
            throw new BizException(errorEnum);
        }
    }

    static public void falseThrowGet(boolean test, Supplier<ErrorEnum> supplier) {
        trueThrowGet(!test, supplier);
    }

    static public void falseThrow(boolean test, ErrorEnum errorEnum) {
        trueThrow(!test, errorEnum);
    }

}
