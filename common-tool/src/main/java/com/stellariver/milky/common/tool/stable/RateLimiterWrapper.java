package com.stellariver.milky.common.tool.stable;

import com.google.common.util.concurrent.RateLimiter;
import com.stellariver.milky.common.tool.exception.ErrorEnumsBase;
import com.stellariver.milky.common.tool.exception.SysException;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Duration;
import java.time.temporal.ChronoUnit;


@SuppressWarnings("all")
@Data
@Builder
@CustomLog
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RateLimiterWrapper {

    String id;

    RateLimiter rateLimiter;

    RlConfig.Strategy strategy;

    Duration timeout;

    Duration warningThreshold;

    public boolean acquire() {
        if (strategy == RlConfig.Strategy.FAIL_FAST) {
            return rateLimiter.tryAcquire();
        } else if (strategy == RlConfig.Strategy.FAIL_TIME_OUT) {
            return rateLimiter.tryAcquire(timeout);
        } else if (strategy == RlConfig.Strategy.FAIL_WAITING) {
            double acquire = rateLimiter.acquire();
            if (warningThreshold != null) {
                Duration minus = warningThreshold.minus((long) (acquire * 1000), ChronoUnit.MILLIS);
                if (minus.isNegative()) {
                    log.arg0(acquire).arg1(rateLimiter.toString()).arg2(warningThreshold).error("WarningThreshold");
                }
            }
            return true;
        } else {
            throw new SysException(ErrorEnumsBase.UNREACHABLE_CODE);
        }
    }

}
