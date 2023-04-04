package com.stellariver.milky.common.tool.util;

import com.stellariver.milky.common.tool.exception.ErrorEnumsBase;
import com.stellariver.milky.common.tool.exception.SysEx;

public class Valid<T> {

    private final Boolean valid;

    private final T data;

    public Valid(Boolean valid, T data) {
        this.valid = valid;
        this.data = data;
    }

    public T get() {
        SysEx.trueThrow(valid == null || !valid, ErrorEnumsBase.NOT_VALID);
        return data;
    }

    public T getOrDefault(T defaultValue) {
        SysEx.nullThrow(valid);
        if (valid) {
            return data;
        } else {
            return defaultValue;
        }
    }

    public boolean valid() {
        SysEx.nullThrow(valid);
        return valid;
    }

    static public <T> Valid<T> build(T t) {
        return new Valid<>(true, t);
    }

    static public Valid<?> unValid = new Valid<>(false, null);

}
