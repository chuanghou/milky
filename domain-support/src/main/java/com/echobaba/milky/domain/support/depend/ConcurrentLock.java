package com.echobaba.milky.domain.support.depend;

import com.echobaba.milky.common.tool.common.BizException;
import com.echobaba.milky.client.base.ErrorCode;
import lombok.SneakyThrows;

public interface ConcurrentLock {

    boolean tryLock(String lockKey, int secondsToExpire);

    boolean unlock(String lockKey);

    @SneakyThrows
    default boolean tryRetryLock(String lockKey, int secondsToExpire, int times, long sleepTime) {
        BizException.nullThrow(lockKey);
        BizException.trueThrow(times <= 1, ErrorCode.PARAM_IS_WRONG);
        BizException.trueThrow(sleepTime > 5000, ErrorCode.PARAM_IS_WRONG);
        do {
            Thread.sleep(sleepTime);
             if (tryLock(lockKey, secondsToExpire)) {
                 return true;
             }
        } while (times-- > 1);
        return false;
    }
}
