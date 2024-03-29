package com.stellariver.milky.common.base;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author houchuang
 */
public class SysEx extends BaseEx {

    public SysEx(Object object) {
        super(Collections.singletonList(ErrorEnumsBase.SYS_EX.message(object)));
    }

    public SysEx(Throwable throwable) {
        super(throwable);
    }

    public SysEx(ErrorEnum errorEnum) {
        super(Collections.singletonList(errorEnum));
    }

    public SysEx(ErrorEnum errorEnum, Throwable t) {
        super(Collections.singletonList(errorEnum), t);
    }

    public SysEx(List<ErrorEnum> errorEnums, Throwable t) {
        super(errorEnums, t);
    }

    static public void anyNullThrow(Object... params) {
        boolean containNullValue = Arrays.stream(params).anyMatch(Objects::isNull);
        if (containNullValue) {
            throw new SysEx(ErrorEnumsBase.PARAM_IS_NULL);
        }
    }

    static public void nullThrow(@Nullable Object param) {
        if (param == null) {
            throw new SysEx(ErrorEnumsBase.PARAM_IS_NULL);
        }
    }

    static public void nullThrow(@Nullable Object param, Object message) {
        if (param == null) {
            throw new SysEx(ErrorEnumsBase.PARAM_IS_NULL.message(message));
        }
    }

    static public void trueThrowGet(boolean test, Supplier<ErrorEnum> supplier) {
        if (test) {
            throw new SysEx(supplier.get());
        }
    }

    static public void trueThrow(boolean test, ErrorEnum errorEnum) {
        if (test) {
            throw new SysEx(errorEnum);
        }
    }

    static public void trueThrow(boolean test, Object object) {
        if (test) {
            throw new SysEx(ErrorEnumsBase.SYS_EX.message(object));
        }
    }

    static public void falseThrow(boolean test, ErrorEnum errorEnum) {
        trueThrow(!test, errorEnum);
    }

    static public void falseThrowGet(boolean test, Supplier<ErrorEnum> supplier) {
        trueThrowGet(!test, supplier);
    }

    static public void falseThrow(boolean test,  Object object) {
        trueThrow(!test, object);
    }

    static public SysEx unreachable() {
        return new SysEx(ErrorEnumsBase.UNREACHABLE_CODE);
    }

    static public void throwUnreachable() {
        throw new SysEx(ErrorEnumsBase.UNREACHABLE_CODE);
    }

}
