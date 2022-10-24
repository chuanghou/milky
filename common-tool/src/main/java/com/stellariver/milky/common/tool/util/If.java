package com.stellariver.milky.common.tool.util;


import com.stellariver.milky.common.tool.common.ErrorEnumsBase;
import com.stellariver.milky.common.tool.common.SysException;

public class If {

    static public void isTrue(boolean test, Runnable runnable) {
        SysException.anyNullThrow(test, ErrorEnumsBase.PARAM_IS_NULL);
        SysException.anyNullThrow(runnable, ErrorEnumsBase.PARAM_IS_NULL);
        if (test) {
            runnable.run();
        }
    }

    static public void isFalse(boolean test, Runnable runnable) {
        If.isTrue(!test, runnable);
    }

    static public void trueOrFalse(boolean test, Runnable trueRunnable, Runnable falseRunnable) {
        SysException.anyNullThrow(test, ErrorEnumsBase.PARAM_IS_NULL);
        SysException.anyNullThrow(trueRunnable, ErrorEnumsBase.PARAM_IS_NULL);
        SysException.anyNullThrow(falseRunnable, ErrorEnumsBase.PARAM_IS_NULL);
        if (test) {
            trueRunnable.run();
        } else {
            falseRunnable.run();
        }
    }
}
