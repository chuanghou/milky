package com.echobaba.milky.common.tool.common;


import com.echobaba.milky.client.base.ErrorCode;

public class IfUtils {

    static public void ifTrue(boolean test, Runnable runnable) {
        BizException.nullThrow(test, ErrorCodeBase.PARAM_IS_NULL);
        BizException.nullThrow(runnable, ErrorCodeBase.PARAM_IS_NULL);
        if (test) {
            runnable.run();
        }
    }

    static public void ifFalse(boolean test, Runnable runnable) {
        IfUtils.ifTrue(!test, runnable);
    }
}
