package com.stellariver.milky.infrastructure.base.mq;

import com.stellariver.milky.common.tool.stable.AbstractStableSupport;
import com.stellariver.milky.common.tool.stable.RateLimiterWrapper;
import lombok.CustomLog;

/**
 * @author houchuang
 */
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
