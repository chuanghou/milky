package com.echobaba.milky.common.tool.common;

import com.echobaba.milky.client.base.ErrorCode;
import com.echobaba.milky.common.tool.utils.Collect;

import java.util.*;
import java.util.function.Supplier;

/**
 */
public class BizException extends RuntimeException {

    private final String code;

    private final List<ErrorCode> errorCodes;

    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.errorCodes = Collections.singletonList(errorCode);
    }
    public BizException(ErrorCode errorCode, Throwable t) {
        super(errorCode.getMessage(), t);
        this.code = errorCode.getCode();
        this.errorCodes = Collections.singletonList(errorCode);
    }

    public BizException(List<ErrorCode> errorCodes, Throwable t) {
        super(errorCodes.get(0).getMessage(), t);
        this.code = errorCodes.get(0).getCode();
        this.errorCodes = errorCodes;
    }

    public String getErrorCode() {
        return this.code;
    }

    public ErrorCode getFirstErrorCode() {
        return errorCodes.get(0);
    }

    public List<ErrorCode> getErrorCodes() {
        return errorCodes;
    }

    static public void nullThrow(Object... params) {
        boolean haveNullValue = Arrays.stream(params).anyMatch(Objects::isNull);
        if (haveNullValue) {
            throw new BizException(ErrorCode.PARAM_IS_NULL);
        }
    }

    static public void nullThrow(Object param, Supplier<ErrorCode> supplier) {
        if (param == null) {
            throw new BizException(supplier.get());
        }
    }

    static public void nullThrow(Object param, ErrorCode errorCode) {
        if (param == null) {
            throw new BizException(errorCode);
        }
    }

    static public void with(ErrorCode errorCode) {
        throw new BizException(errorCode);
    }

    static public void trueThrow(boolean test, Supplier<ErrorCode> supplier) {
        if (test) {
            throw new BizException(supplier.get());
        }
    }

    static public void emptyThrow(Collection<?> collection, ErrorCode errorCode) {
        if (Collect.isEmpty(collection)) {
            throw new BizException(errorCode);
        }
    }

    static public void trueThrow(boolean test, ErrorCode errorCode) {
        if (test) {
            throw new BizException(errorCode);
        }
    }

    static public void falseThrow(boolean test, Supplier<ErrorCode> supplier) {
        if (!test) {
            throw new BizException(supplier.get());
        }
    }

    static public void falseThrow(boolean test, ErrorCode errorCode) {
        if (!test) {
            throw new BizException(errorCode);
        }
    }
}
