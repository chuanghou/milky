package com.stellariver.milky.common.tool.common;

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
}
