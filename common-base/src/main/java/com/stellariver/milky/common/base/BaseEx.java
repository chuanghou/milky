package com.stellariver.milky.common.base;


import java.util.Collections;
import java.util.List;

/**
 * @author houchuang
 */
public abstract class BaseEx extends RuntimeException {

    private final List<ErrorEnum> errorEnums;

    public BaseEx(Throwable t) {
        super(t);
        this.errorEnums = Collections.singletonList(ErrorEnumsBase.SYS_EX.message(t.getMessage()));
    }

    public BaseEx(Throwable t, boolean fillStackTrace) {
        super(null, t, true, fillStackTrace);
        this.errorEnums = Collections.singletonList(ErrorEnumsBase.SYS_EX.message(t.getMessage()));
    }

    public BaseEx(List<ErrorEnum> errorEnums) {
        super(errorEnums.get(0).getMessage());
        this.errorEnums = errorEnums;
    }

    public BaseEx(List<ErrorEnum> errorEnums, boolean fillStackTrace) {
        super(errorEnums.get(0).getMessage(), null, true, fillStackTrace);
        this.errorEnums = errorEnums;
    }

    public BaseEx(List<ErrorEnum> errorEnums, Throwable t) {
        super(errorEnums.get(0).getMessage(), t);
        this.errorEnums = errorEnums;
    }

    public BaseEx(List<ErrorEnum> errorEnums, Throwable t, boolean fillStackTrace) {
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
