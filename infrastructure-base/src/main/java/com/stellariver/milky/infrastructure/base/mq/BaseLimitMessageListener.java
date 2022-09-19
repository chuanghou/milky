package com.stellariver.milky.infrastructure.base.mq;

import com.alibaba.csp.sentinel.SphO;
import com.stellariver.milky.common.tool.common.LogChoice;
import com.stellariver.milky.infrastructure.base.LogConfiguration;
import lombok.CustomLog;
import lombok.SneakyThrows;

import java.util.concurrent.TimeUnit;

@CustomLog
public abstract class BaseLimitMessageListener implements LogConfiguration {

    @SneakyThrows
    protected void flowControl() {
        try {
            while (Boolean.FALSE.equals(SphO.entry(this.getClass().getName()))) {
                TimeUnit.MILLISECONDS.sleep(100);
            }
        } finally {
            SphO.exit();
        }
    }

    public LogChoice logChoice() {
        return LogChoice.ALWAYS;
    }

    public void finalWork() {

    }

}
