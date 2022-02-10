package com.echobaba.milky.common.tool.common;

import com.alibaba.c2m.milky.client.base.BizException;
import com.alibaba.c2m.milky.client.base.ErrorCode;

public class IfUtils {

    static public void ifTrue(boolean test, Runnable runnable) {
        BizException.nullThrow(test, ErrorCode.PARAM_IS_NULL);
        BizException.nullThrow(runnable, ErrorCode.PARAM_IS_NULL);
        if (test) {
            runnable.run();
        }
    }

    static public void ifFalse(boolean test, Runnable runnable) {
        IfUtils.ifTrue(!test, runnable);
    }
}
