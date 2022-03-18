package com.stellariver.milky.common.tool.common;

public class ExceptionUtil {

    public static void trueThrow(boolean test, String message) {
        if (test) {
            throw new RuntimeException(message);
        }
    }

    public static void falseThrow(boolean test, String message) {
        trueThrow(!test, message);
    }
}
