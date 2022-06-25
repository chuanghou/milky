package com.stellariver.milky.common.tool.common;

import com.stellariver.milky.common.base.Error;

import java.util.Collections;
import java.util.List;

public abstract class BaseException extends RuntimeException {

    private final List<Error> errors;

    public BaseException(Throwable t) {
        super(t);
        this.errors = Collections.singletonList(ErrorEnumBase.UNDEFINED.message(t.getMessage()));
    }

    public BaseException(List<Error> errors) {
        super(errors.get(0).getMessage());
        this.errors = errors;
    }

    public BaseException(List<Error> errors, Throwable t) {
        super(errors.get(0).getMessage(), t);
        this.errors = errors;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public Error getFirstError() {
        return errors.get(0);
    }

}
