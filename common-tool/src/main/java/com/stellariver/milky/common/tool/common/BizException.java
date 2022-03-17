package com.stellariver.milky.common.tool.common;

import com.stellariver.milky.common.base.ErrorCode;
import com.stellariver.milky.common.tool.util.Collect;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 */
public class BizException extends RuntimeException {

    private final String errorCode;

    private final List<ErrorCode> errorCodes;

    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode.getCode();
        this.errorCodes = Collections.singletonList(errorCode);
    }
    public BizException(ErrorCode errorCode, Throwable t) {
        super(errorCode.getMessage(), t);
        this.errorCode = errorCode.getCode();
        this.errorCodes = Collections.singletonList(errorCode);
    }

    public BizException(List<ErrorCode> errorCodes, Throwable t) {
        super(errorCodes.get(0).getMessage(), t);
        this.errorCode = errorCodes.get(0).getCode();
        this.errorCodes = errorCodes;
    }

    public String getErrorCode() {
        return this.errorCode;
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
            throw new BizException(ErrorCodeEnumBase.PARAM_IS_NULL);
        }
    }

    static public <T, K> void nullThrow(T param, Function<T, K> deepFinder) {
        if (param == null) {
            throw new BizException(ErrorCodeEnumBase.PARAM_IS_NULL);
        }
        if (deepFinder.apply(param) == null) {
            throw new BizException(ErrorCodeEnumBase.PARAM_IS_NULL);
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

    static private ThreadLocal<List<ErrorCode>>  temporaryErrorCodes;

    /**
     * 对于任何一个init函数都必须在有try-final块里面有 removeTemporaryErrorCodes（）操作
     */
    static public void initTemporaryErrorCodes() {
        temporaryErrorCodes = new ThreadLocal<>();
        temporaryErrorCodes.set(new ArrayList<>());
    }

    static public void removeTemporaryErrorCodes() {
        Optional.ofNullable(temporaryErrorCodes).ifPresent(ThreadLocal::remove);
    }

    static public void addTemporaryErrorCode(ErrorCode errorCode) {
        Optional.ofNullable(temporaryErrorCodes)
                .map(ThreadLocal::get)
                .orElseThrow(() -> new BizException(ErrorCodeEnumBase
                        .CONFIG_ERROR.message("temporaryErrorCodes container need explicitly init!")))
                .add(errorCode);
    }

    static public List<ErrorCode> getTemporaryErrorCodes() {
        return Optional.ofNullable(temporaryErrorCodes).map(ThreadLocal::get).orElse(new ArrayList<>());
    }

}
