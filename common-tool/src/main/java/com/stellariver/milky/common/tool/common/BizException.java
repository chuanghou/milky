package com.stellariver.milky.common.tool.common;

import com.stellariver.milky.common.base.Error;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.common.tool.util.Json;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 */
public class BizException extends BaseException {

    private final String errorCode;

    private final List<Error> errors;

    public BizException(Error error) {
        super(error.getMessage());
        this.errorCode = error.getCode();
        this.errors = Collections.singletonList(error);
    }
    public BizException(Error error, Throwable t) {
        super(error.getMessage(), t);
        this.errorCode = error.getCode();
        this.errors = Collections.singletonList(error);
    }

    public BizException(List<Error> errors, Throwable t) {
        super(errors.get(0).getMessage(), t);
        this.errorCode = errors.get(0).getCode();
        this.errors = errors;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public Error getFirstError() {
        return errors.get(0);
    }

    public List<Error> getErrors() {
        return errors;
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
