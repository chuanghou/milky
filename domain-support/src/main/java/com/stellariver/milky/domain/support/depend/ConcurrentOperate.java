package com.stellariver.milky.domain.support.depend;

import com.stellariver.milky.common.tool.common.BizException;
import com.stellariver.milky.common.tool.common.ErrorCodeEnumBase;
import com.stellariver.milky.common.tool.common.ExceptionUtil;
import com.stellariver.milky.domain.support.command.Command;
import lombok.SneakyThrows;

public interface ConcurrentOperate {

    void sendOrderly(Command command);

    void receiveCommand(Command command);

    boolean tryLock(String lockKey, int secondsToExpire);

    boolean unlock(String lockKey);


    @SneakyThrows
    default boolean tryRetryLock(String lockKey, int secondsToExpire, int times, long sleepTime) {
        ExceptionUtil.nullThrow(lockKey);
        ExceptionUtil.trueThrow(times <= 0, () -> "retry times should not smaller than 0 or equal with 0");
        ExceptionUtil.trueThrow(sleepTime > 5000, () -> "sleep time is too long");
        while (times-- > 0) {
            Thread.sleep(sleepTime);
            if (tryLock(lockKey, secondsToExpire)) {
                return true;
            }
        }
        return false;
    }
}
