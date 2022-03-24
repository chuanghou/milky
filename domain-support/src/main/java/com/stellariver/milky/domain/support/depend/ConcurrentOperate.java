package com.stellariver.milky.domain.support.depend;

import com.stellariver.milky.common.tool.common.SysException;
import com.stellariver.milky.domain.support.command.Command;
import lombok.SneakyThrows;

public interface ConcurrentOperate {

    void sendOrderly(Command command);

    void receiveCommand(Command command);

    boolean tryLock(String lockKey, String encryptionKey, int secondsToExpire);

    boolean unlock(String lockKey, String encryptionKey);


    @SneakyThrows
    default boolean tryRetryLock(String lockKey, String encryptionKey, int secondsToExpire, int times, long sleepTime) {
        SysException.nullThrow(lockKey);
        SysException.trueThrow(times <= 0, "retry times should not smaller than 0 or equal with 0");
        SysException.trueThrow(sleepTime > 5000, "sleep time is too long");
        while (times-- > 0) {
            Thread.sleep(sleepTime);
            if (tryLock(lockKey, encryptionKey, secondsToExpire)) {
                return true;
            }
        }
        return false;
    }
}
