package com.stellariver.milky.infrastructure.base.mq;

import com.alibaba.csp.sentinel.SphO;
import com.stellariver.milky.common.tool.stable.AbstractStableSupport;
import com.stellariver.milky.common.tool.stable.RateLimiterWrapper;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.concurrent.TimeUnit;

@CustomLog
public abstract class BaseLimitMessageListener {

    protected void flowControl() {
        AbstractStableSupport abstractStableSupport = AbstractStableSupport.abstractStableSupport;
        if (abstractStableSupport == null) {
            return;
        }
        String key = this.getClass().getName();
        RateLimiterWrapper rateLimiterWrapper = abstractStableSupport.rateLimiter(key);
        if (rateLimiterWrapper != null) {
            rateLimiterWrapper.acquire();
        }
    }

    public boolean alwaysLog() {
        return true;
    }

    public void finalWork() {

    }

}
