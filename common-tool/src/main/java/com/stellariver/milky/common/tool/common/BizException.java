package com.stellariver.milky.common.tool.common;

import com.stellariver.milky.common.base.Code;
import com.stellariver.milky.common.tool.util.Collect;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 */
public class BizException extends RuntimeException {

    private final String errorCode;

    private final List<Code> errorCodes;

    public BizException(Code code) {
        super(code.getMessage());
        this.errorCode = code.getCode();
        this.errorCodes = Collections.singletonList(code);
    }
    public BizException(Code code, Throwable t) {
        super(code.getMessage(), t);
        this.errorCode = code.getCode();
        this.errorCodes = Collections.singletonList(code);
    }

    public BizException(List<Code> errorCodes, Throwable t) {
        super(errorCodes.get(0).getMessage(), t);
        this.errorCode = errorCodes.get(0).getCode();
        this.errorCodes = errorCodes;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public Code getFirstErrorCode() {
        return errorCodes.get(0);
    }

    public List<Code> getErrorCodes() {
        return errorCodes;
    }

    static public void nullThrow(Object... params) {
        boolean haveNullValue = Arrays.stream(params).anyMatch(Objects::isNull);
        if (haveNullValue) {
            throw new BizException(CodeEnumBase.PARAM_IS_NULL);
        }
    }

    static public <T, K> void nullThrow(T param, Function<T, K> deepFinder) {
        if (param == null) {
            throw new BizException(CodeEnumBase.PARAM_IS_NULL);
        }
        if (deepFinder.apply(param) == null) {
            throw new BizException(CodeEnumBase.PARAM_IS_NULL);
        }
    }

    static public void nullThrow(Object param, Supplier<Code> supplier) {
        if (param == null) {
            throw new BizException(supplier.get());
        }
    }

    static public void nullThrow(Object param, Code code) {
        if (param == null) {
            throw new BizException(code);
        }
    }

    static public void with(Code code) {
        throw new BizException(code);
    }

    static public void trueThrow(boolean test, Supplier<Code> supplier) {
        if (test) {
            throw new BizException(supplier.get());
        }
    }

    static public void emptyThrow(Collection<?> collection, Supplier<Code> supplier) {
        if (Collect.isEmpty(collection)) {
            throw new BizException(supplier.get());
        }
    }

    static public void falseThrow(boolean test, Supplier<Code> supplier) {
        if (!test) {
            throw new BizException(supplier.get());
        }
    }

    static private ThreadLocal<List<Code>>  temporaryErrorCodes;

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

    static public void addTemporaryErrorCode(Code code) {
        Optional.ofNullable(temporaryErrorCodes)
                .map(ThreadLocal::get)
                .orElseThrow(() -> new BizException(CodeEnumBase
                        .CONFIG_ERROR.message("temporaryErrorCodes container need explicitly init!")))
                .add(code);
    }

    static public List<Code> getTemporaryErrorCodes() {
        return Optional.ofNullable(temporaryErrorCodes).map(ThreadLocal::get).orElse(new ArrayList<>());
    }

}
