package com.stellariver.milky.common.tool.common;


public class If {

    static public void isTrue(boolean test, Runnable runnable) {
        SysException.nullThrow(test, ErrorEnumBase.PARAM_IS_NULL);
        SysException.nullThrow(runnable, ErrorEnumBase.PARAM_IS_NULL);
        if (test) {
            runnable.run();
        }
    }

    static public void isFalse(boolean test, Runnable runnable) {
        If.isTrue(!test, runnable);
    }

    static public void trueOrFalse(boolean test, Runnable trueRunnable, Runnable falseRunnable) {
        SysException.nullThrow(test, ErrorEnumBase.PARAM_IS_NULL);
        SysException.nullThrow(trueRunnable, ErrorEnumBase.PARAM_IS_NULL);
        SysException.nullThrow(falseRunnable, ErrorEnumBase.PARAM_IS_NULL);
        if (test) {
            trueRunnable.run();
        } else {
            falseRunnable.run();
        }
    }
}
