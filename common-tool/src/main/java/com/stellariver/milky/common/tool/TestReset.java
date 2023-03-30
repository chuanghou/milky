package com.stellariver.milky.common.tool;

import com.stellariver.milky.common.base.Result;
import com.stellariver.milky.common.base.TraceIdGetter;
import com.stellariver.milky.common.tool.common.BeanUtil;
import com.stellariver.milky.common.tool.common.Runner;

public class TestReset {

    static public void reset() {

        BeanUtil.setBeanLoader(null);
        Runner.reset();
        Result.setTraceIdGetter(null);
    }

}
