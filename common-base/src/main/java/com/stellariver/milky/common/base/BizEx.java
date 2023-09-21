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
public class BizEx extends BaseEx {

    public BizEx(ErrorEnum errorEnum, Throwable throwable) {
        super(Collections.singletonList(errorEnum), throwable);
    }

    public BizEx(ErrorEnum errorEnum, Throwable throwable, boolean fillStackTrace) {
        super(Collections.singletonList(errorEnum), throwable, fillStackTrace);
    }

    public BizEx(ErrorEnum errorEnum) {
        super(Collections.singletonList(errorEnum));
    }

    public BizEx(ErrorEnum errorEnum, boolean fillStackTrace) {
        super(Collections.singletonList(errorEnum), fillStackTrace);
    }

    public BizEx(List<ErrorEnum> errorEnums) {
        super(errorEnums);
    }

    public BizEx(List<ErrorEnum> errorEnums, boolean fillStackTrace) {
        super(errorEnums, fillStackTrace);
    }

    static public void anyNullThrow(Object... params) {
        boolean containNullValue = Arrays.stream(params).anyMatch(Objects::isNull);
        if (containNullValue) {
            throw new BizEx(ErrorEnumsBase.PARAM_IS_NULL);
        }
    }

    static public void nullThrow(@Nullable Object param) {
        if (param == null) {
            throw new BizEx(ErrorEnumsBase.PARAM_IS_NULL);
        }
    }

    static public void nullThrow(@Nullable Object param, Object message) {
        if (param == null) {
            throw new BizEx(ErrorEnumsBase.PARAM_IS_NULL.message(message));
        }
    }


    static public void trueThrowGet(boolean test, Supplier<ErrorEnum> supplier) {
        if (test) {
            throw new BizEx(supplier.get());
        }
    }

    static public void trueThrowGet(boolean test, Supplier<ErrorEnum> supplier, boolean fillStackTrace) {
        if (test) {
            throw new BizEx(supplier.get(), fillStackTrace);
        }
    }

    static public void trueThrow(boolean test, ErrorEnum errorEnum) {
        if (test) {
            throw new BizEx(errorEnum);
        }
    }

    static public void trueThrow(boolean test, ErrorEnum errorEnum, boolean fillStackTrace) {
        if (test) {
            throw new BizEx(errorEnum, fillStackTrace);
        }
    }

    static public void falseThrowGet(boolean test, Supplier<ErrorEnum> supplier) {
        trueThrowGet(!test, supplier);
    }

    static public void falseThrowGet(boolean test, Supplier<ErrorEnum> supplier, boolean fillStackTrace) {
        trueThrowGet(!test, supplier, fillStackTrace);
    }

    static public void falseThrow(boolean test, ErrorEnum errorEnum) {
        trueThrow(!test, errorEnum);
    }

    static public void falseThrow(boolean test, ErrorEnum errorEnum, boolean fillStackTrace) {
        trueThrow(!test, errorEnum, fillStackTrace);
    }

}
