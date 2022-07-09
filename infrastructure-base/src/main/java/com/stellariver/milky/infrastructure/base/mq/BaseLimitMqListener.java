package com.stellariver.milky.infrastructure.base.mq;

import com.alibaba.csp.sentinel.SphO;
import lombok.CustomLog;
import lombok.SneakyThrows;

import java.util.concurrent.TimeUnit;

@CustomLog
public class BaseLimitMqListener {

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
}
