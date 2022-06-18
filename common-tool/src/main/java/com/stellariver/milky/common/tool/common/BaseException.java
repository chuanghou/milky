package com.stellariver.milky.common.tool.common;

import com.stellariver.milky.common.base.Error;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class BaseException extends RuntimeException {

    private final List<Error> errors;

    public BaseException(Error error) {
        super(error.getMessage());
        this.errors = Collections.singletonList(error);
    }

    public BaseException(Error error, Throwable t) {
        super(error.getMessage(), t);
        this.errors = Collections.singletonList(error);
    }

    public BaseException(List<Error> errors, Throwable t) {
        super(errors.get(0).getMessage(), t);
        this.errors = errors;
    }

    public Error getFirstError() {
        return errors.get(0);
    }

}
