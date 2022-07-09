package com.stellariver.milky.common.tool.common;

import com.stellariver.milky.common.base.Error;
import com.stellariver.milky.common.tool.util.Collect;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 */
public class BizException extends BaseException {

    public BizException(Error error, Throwable throwable) {
        super(Collections.singletonList(error), throwable);
    }

    public BizException(Error error) {
        super(Collections.singletonList(error));
    }

    public BizException(List<Error> errors) {
        super(errors);
    }

    static public void anyNullThrow(Object... params) {
        boolean containNullValue = Arrays.stream(params).anyMatch(Objects::isNull);
        if (containNullValue) {
            throw new BizException(ErrorEnumBase.PARAM_IS_NULL);
        }
    }

    static public void nullThrow(Object param, Supplier<Error> supplier) {
        if (param == null) {
            throw new BizException(supplier.get());
        }
    }

    static public void trueThrowGet(boolean test, Supplier<Error> supplier) {
        if (test) {
            throw new BizException(supplier.get());
        }
    }

    static public void trueThrow(boolean test, Error error) {
        if (test) {
            throw new BizException(error);
        }
    }

    static public void falseThrowGet(boolean test, Supplier<Error> supplier) {
        trueThrowGet(!test, supplier);
    }

    static public void falseThrow(boolean test, Error error) {
        trueThrow(!test, error);
    }

}
