package com.stellariver.milky.spring.partner;

import com.stellariver.milky.common.base.BeanLoader;
import com.stellariver.milky.common.base.BeanUtil;
import com.stellariver.milky.common.base.Result;
import com.stellariver.milky.common.base.TraceIdGetter;
import com.stellariver.milky.common.tool.common.Runner;
import com.stellariver.milky.common.tool.stable.MilkyStableSupport;
import com.stellariver.milky.common.tool.util.RunnerExtension;

import java.util.Optional;


public class StaticSupport {

    public StaticSupport(MilkyStableSupport milkyStableSupport,
                         RunnerExtension runnerExtension,
                         TraceIdGetter traceIdGetter,
                         BeanLoader beanLoader) {
        Optional.ofNullable(milkyStableSupport).ifPresent(Runner::setMilkyStableSupport);
        Optional.ofNullable(runnerExtension).ifPresent(Runner::setFailureExtendable);
        Optional.ofNullable(traceIdGetter).ifPresent(Result::initTraceIdGetter);
        BeanUtil.setBeanLoader(beanLoader);
    }

    public void close() {
        Runner.setMilkyStableSupport(null);
        Runner.setFailureExtendable(null);
        Result.initTraceIdGetter(null);
        BeanUtil.setBeanLoader(null);
    }

}
