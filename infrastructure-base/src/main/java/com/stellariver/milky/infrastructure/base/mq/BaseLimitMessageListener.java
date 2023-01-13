package com.stellariver.milky.infrastructure.base.mq;

import com.stellariver.milky.common.tool.stable.MilkyStableSupport;
import com.stellariver.milky.common.tool.stable.RateLimiterWrapper;
import lombok.CustomLog;

/**
 * @author houchuang
 */
@CustomLog
public abstract class BaseLimitMessageListener {

    protected void flowControl() {
        MilkyStableSupport milkyStableSupport = getMilkyStableSupport();
        if (milkyStableSupport == null) {
            return;
        }
        String key = this.getClass().getName();
        RateLimiterWrapper rateLimiterWrapper = milkyStableSupport.rateLimiter(key);
        if (rateLimiterWrapper != null) {
            rateLimiterWrapper.acquire();
        }
    }

    public boolean alwaysLog() {
        return true;
    }

    public void finalWork() {

    }

    abstract MilkyStableSupport getMilkyStableSupport();

}
