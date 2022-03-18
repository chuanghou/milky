package com.stellariver.milky.common.tool.common;

public class ExceptionUtil {

    static void trueThrow(boolean test, String message) {
        if (test) {
            throw new RuntimeException(message);
        }
    }
}
