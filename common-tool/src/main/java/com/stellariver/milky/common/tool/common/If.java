package com.stellariver.milky.common.tool.common;


public class If {

    static public void isTrue(boolean test, Runnable runnable) {
        BizException.nullThrow(test, CodeEnumBase.PARAM_IS_NULL);
        BizException.nullThrow(runnable, CodeEnumBase.PARAM_IS_NULL);
        if (test) {
            runnable.run();
        }
    }

    static public void isFalse(boolean test, Runnable runnable) {
        If.isTrue(!test, runnable);
    }

    static public void trueOrFalse(boolean test, Runnable trueRunnable, Runnable falseRunnable) {
        BizException.nullThrow(test, CodeEnumBase.PARAM_IS_NULL);
        BizException.nullThrow(trueRunnable, CodeEnumBase.PARAM_IS_NULL);
        BizException.nullThrow(falseRunnable, CodeEnumBase.PARAM_IS_NULL);
        if (test) {
            trueRunnable.run();
        } else {
            falseRunnable.run();
        }
    }
}
