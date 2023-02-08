package com.stellariver.milky.common.tool.exception;

import com.stellariver.milky.common.base.ErrorEnum;
import com.stellariver.milky.common.tool.util.Collect;

import java.util.List;

/**
 * @author houchuang
 */
public abstract class BaseException extends RuntimeException {

    private final List<ErrorEnum> errorEnums;

    public BaseException(Throwable t) {
        super(t);
        this.errorEnums = Collect.asList(ErrorEnumsBase.SYSTEM_EXCEPTION.message(t.getMessage()));
    }

    public BaseException(Throwable t, boolean fillStackTrace) {
        super(null, t, true, fillStackTrace);
        this.errorEnums = Collect.asList(ErrorEnumsBase.SYSTEM_EXCEPTION.message(t.getMessage()));
    }

    public BaseException(List<ErrorEnum> errorEnums) {
        super(errorEnums.get(0).getMessage());
        this.errorEnums = errorEnums;
    }

    public BaseException(List<ErrorEnum> errorEnums, boolean fillStackTrace) {
        super(errorEnums.get(0).getMessage(), null, true, fillStackTrace);
        this.errorEnums = errorEnums;
    }

    public BaseException(List<ErrorEnum> errorEnums, Throwable t) {
        super(errorEnums.get(0).getMessage(), t);
        this.errorEnums = errorEnums;
    }

    public BaseException(List<ErrorEnum> errorEnums, Throwable t, boolean fillStackTrace) {
        super(errorEnums.get(0).getMessage(), t, true, fillStackTrace);
        this.errorEnums = errorEnums;
    }

    public List<ErrorEnum> getErrors() {
        return errorEnums;
    }

    public ErrorEnum getFirstError() {
        return errorEnums.get(0);
    }

}
