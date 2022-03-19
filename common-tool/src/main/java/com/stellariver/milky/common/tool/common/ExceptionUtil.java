package com.stellariver.milky.common.tool.common;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

public class ExceptionUtil {

    public static void trueThrow(boolean test, Supplier<String> supplier) {
        if (test) {
            throw new RuntimeException(supplier.get());
        }
    }

    public static void falseThrow(boolean test, Supplier<String> supplier) {
        trueThrow(!test, supplier);
    }

    public static void nullThrow(Object object, Supplier<String> supplier) {
        if (object == null) {
            throw new RuntimeException(supplier.get());
        }
    }

    public static void nullThrow(Object... objects) {
        long count = Arrays.stream(objects).filter(Objects::isNull).count();
        if (count > 0) {
            throw new RuntimeException("param is null");
        }
    }
}
