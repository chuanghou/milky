package com.stellariver.milky.domain.support.dependency;

import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.common.NameSpace;
import com.stellariver.milky.common.tool.common.SysException;
import com.stellariver.milky.domain.support.ErrorEnums;
import com.stellariver.milky.domain.support.base.RetryParameter;
import lombok.CustomLog;
import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.Map;

@CustomLog
public abstract class ConcurrentOperate {

    private final ThreadLocal<Map<String, Integer>> lockedKeys = ThreadLocal.withInitial(HashMap::new);

    public boolean tryReentrantLock(NameSpace nameSpace, String lockKey, String encryptionKey, int milsToExpire) {
        String key = nameSpace.preFix(lockKey);
        boolean contains = lockedKeys.get().containsKey(nameSpace.preFix(lockKey));

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

    abstract protected boolean tryLock(NameSpace nameSpace, String lockKey, String encryptionKey, int milsToExpire);

    public boolean unReentrantLock(NameSpace nameSpace, String lockKey, String encryptionKey) {
        String key = nameSpace.preFix(lockKey);
        boolean contains = lockedKeys.get().containsKey(nameSpace.preFix(lockKey));
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

    abstract protected boolean unlock(NameSpace nameSpace, String lockKey, String encryptionKey);

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
