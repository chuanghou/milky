package com.stellariver.milky.common.tool.util;


import com.stellariver.milky.common.base.ErrorEnumsBase;
import com.stellariver.milky.common.base.SysEx;

/**
 * @author houchuang
 */
public class If {

    static public void isTrue(boolean test, Runnable runnable) {
        SysEx.anyNullThrow(test, ErrorEnumsBase.PARAM_IS_NULL);
        SysEx.anyNullThrow(runnable, ErrorEnumsBase.PARAM_IS_NULL);
        if (test) {
            runnable.run();
        }
    }

    static public void isFalse(boolean test, Runnable runnable) {
        If.isTrue(!test, runnable);
    }

    static public void trueOrFalse(boolean test, Runnable trueRunnable, Runnable falseRunnable) {
        SysEx.anyNullThrow(test, ErrorEnumsBase.PARAM_IS_NULL);
        SysEx.anyNullThrow(trueRunnable, ErrorEnumsBase.PARAM_IS_NULL);
        SysEx.anyNullThrow(falseRunnable, ErrorEnumsBase.PARAM_IS_NULL);
        if (test) {
            trueRunnable.run();
        } else {
            falseRunnable.run();
        }
    }
}
