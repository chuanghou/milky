package com.stellariver.milky.domain.support.dependency;

import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.exception.SysException;
import com.stellariver.milky.common.tool.common.UK;
import com.stellariver.milky.domain.support.ErrorEnums;
import com.stellariver.milky.domain.support.base.RetryParameter;
import lombok.CustomLog;
import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.Map;

@CustomLog
public abstract class ConcurrentOperate {

    private final ThreadLocal<Map<String, Integer>> lockedKeys = ThreadLocal.withInitial(HashMap::new);

    public boolean tryReentrantLock(UK nameSpace, String lockKey, String encryptionKey, int milsToExpire) {
        String key = nameSpace.preFix(lockKey);
        boolean contains = lockedKeys.get().containsKey(key);
        if (!contains) {
            boolean locked = tryLock(nameSpace, lockKey, encryptionKey, milsToExpire);
            if (!locked) {
                return false;
            }
            lockedKeys.get().put(key, 0);
        }

        Integer lockedTimes = lockedKeys.get().get(key);
        lockedKeys.get().put(key, ++lockedTimes);
        return true;
    }

    public boolean unReentrantLock(UK nameSpace, String lockKey, String encryptionKey) {
        String key = nameSpace.getKey() + "_" + lockKey;
        boolean contains = lockedKeys.get().containsKey(key);
        SysException.falseThrow(contains, ErrorEnums.SYSTEM_EXCEPTION.message(key));
        Integer lockedTimes = lockedKeys.get().get(key);
        lockedTimes--;
        if (Kit.eq(lockedTimes, 0)) {
            boolean unlock = unlock(nameSpace, lockKey, encryptionKey);
            if (!unlock) {
                log.arg0(nameSpace).arg1(lockKey).arg2(encryptionKey).error("UNLOCK_FAILURE");
            }
            lockedKeys.get().remove(key);
        } else {
            lockedKeys.get().put(key, lockedTimes);
        }
        return true;
    }

    abstract protected boolean tryLock(UK nameSpace, String lockKey, String encryptionKey, int milsToExpire);

    abstract protected boolean unlock(UK nameSpace, String lockKey, String encryptionKey);

    @SneakyThrows
    public boolean tryRetryLock(RetryParameter retryParameter) {
        SysException.anyNullThrow(retryParameter.getLockKey());
        SysException.trueThrow(retryParameter.getTimes() <= 0, "retry times should not smaller than 0 or equal with 0");
        SysException.trueThrow(retryParameter.getSleepTimeMils() > 5000, "sleep time is too long");
        int times = retryParameter.getTimes();
        String lockKey = retryParameter.getLockKey();
        String encryptionKey = retryParameter.getEncryptionKey();
        int milsToExpire = retryParameter.getMilsToExpire();
        while (times-- > 0) {
            Thread.sleep(retryParameter.getSleepTimeMils());
            if (tryReentrantLock(retryParameter.getNameSpace(), lockKey, encryptionKey, milsToExpire)) {
                return true;
            }
        }
        return false;
    }
}
