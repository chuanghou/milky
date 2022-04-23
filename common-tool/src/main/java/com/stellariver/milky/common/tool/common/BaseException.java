package com.stellariver.milky.common.tool.common;

import com.stellariver.milky.common.base.Error;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BaseException extends RuntimeException {


    protected BaseException(String message) {
        super(message);
    }

    protected BaseException(String message, Throwable cause) {
        super(message, cause);
    }

    protected BaseException(Throwable cause) {
        super(cause);
    }

    static private ThreadLocal<List<Error>> temporaryErrors;

    /**
     * 对于任何一个init函数都必须在有try-final块里面有 removeTemporaryErrorCodes（）操作
     */
    static public void initTemporaryErrors() {
        temporaryErrors = new ThreadLocal<>();
        temporaryErrors.set(new ArrayList<>());
    }

    static public void removeTemporaryErrors() {
        Optional.ofNullable(temporaryErrors).ifPresent(ThreadLocal::remove);
    }

    static public void addTemporaryError(Error error) {
        Optional.ofNullable(temporaryErrors)
                .map(ThreadLocal::get)
                .orElseThrow(() -> new BizException(ErrorEnumBase
                        .CONFIG_ERROR.message("temporaryErrorCodes container need explicitly init!")))
                .add(error);
    }

    static public List<Error> getTemporaryErrors() {
        return Optional.ofNullable(temporaryErrors).map(ThreadLocal::get).orElse(new ArrayList<>());
    }

}
