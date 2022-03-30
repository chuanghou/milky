package com.stellariver.milky.domain.support.dependency;

import com.stellariver.milky.common.tool.common.SysException;
import com.stellariver.milky.domain.support.base.RetryParameter;
import com.stellariver.milky.domain.support.command.Command;
import lombok.SneakyThrows;

public interface ConcurrentOperate {

    void sendOrderly(Command command);

    void receiveCommand(Command command);

    boolean tryLock(String lockKey, String encryptionKey, int milsToExpire);

    boolean unlock(String lockKey, String encryptionKey);


    @SneakyThrows
    default boolean tryRetryLock(RetryParameter retryParameter) {
        SysException.nullThrow(retryParameter.getLockKey());
        SysException.trueThrow(retryParameter.getTimes() <= 0, "retry times should not smaller than 0 or equal with 0");
        SysException.trueThrow(retryParameter.getSleepTimeMils() > 5000, "sleep time is too long");
        int times = retryParameter.getTimes();
        String lockKey = retryParameter.getLockKey();
        String encryptionKey = retryParameter.getEncryptionKey();
        int milsToExpire = retryParameter.getMilsToExpire();
        while (times-- > 0) {
            Thread.sleep(retryParameter.getSleepTimeMils());
            if (tryLock(lockKey, encryptionKey, milsToExpire)) {
                return true;
            }
        }
        return false;
    }
}
