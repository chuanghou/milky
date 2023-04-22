package com.stellariver.milky.spring.partner;

import com.stellariver.milky.common.base.Result;
import com.stellariver.milky.common.tool.common.BeanUtil;
import com.stellariver.milky.common.tool.common.Runner;


public class StaticSupport {

    public void close() {
        Runner.setMilkyStableSupport(null);
        Runner.setFailureExtendable(null);
        Result.setTraceIdGetter(null);
        BeanUtil.setBeanLoader(null);
    }

}
