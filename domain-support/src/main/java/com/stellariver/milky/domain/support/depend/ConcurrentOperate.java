package com.stellariver.milky.domain.support.depend;

import com.stellariver.milky.common.tool.common.BizException;
import com.stellariver.milky.common.tool.common.ErrorCodeBase;
import com.stellariver.milky.domain.support.command.Command;
import lombok.SneakyThrows;

public interface ConcurrentOperate {

    void sendOrderly(Command command);

    void receiveCommand(Command command);

    boolean tryLock(String lockKey, int secondsToExpire);

    boolean unlock(String lockKey);


    @SneakyThrows
    default boolean tryRetryLock(String lockKey, int secondsToExpire, int times, long sleepTime) {
        BizException.nullThrow(lockKey);
        BizException.trueThrow(times <= 1, ErrorCodeBase.PARAM_IS_WRONG);
        BizException.trueThrow(sleepTime > 5000, ErrorCodeBase.PARAM_IS_WRONG);
        do {
            Thread.sleep(sleepTime);
            if (tryLock(lockKey, secondsToExpire)) {
                return true;
            }
        } while (times-- > 1);
        return false;
    }
}
