package com.stellariver.milky.domain.support.dependency;

import com.stellariver.milky.common.tool.exception.SysException;
import com.stellariver.milky.common.tool.common.UK;
import com.stellariver.milky.domain.support.base.RetryParameter;
import lombok.CustomLog;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

/**
 * @author houchuang
 */
@CustomLog
public abstract class ConcurrentOperate {

    private final ThreadLocal<Map<Pair<UK, String>, String>> lockedKeys = ThreadLocal.withInitial(HashMap::new);

    public boolean tryReentrantLock(UK nameSpace, String lockKey, String encryptionKey, int milsToExpire) {
        boolean contains = lockedKeys.get().containsKey(Pair.of(nameSpace, lockKey));
        if (!contains) {
            boolean locked = tryLock(nameSpace, lockKey, encryptionKey, milsToExpire);
            if (!locked) {
                return false;
            }
            lockedKeys.get().put(Pair.of(nameSpace, lockKey), encryptionKey);
        }
        return true;
    }

    public boolean unLockAll() {
        boolean result = true;
        for (Map.Entry<Pair<UK, String>, String> e: lockedKeys.get().entrySet()) {
            UK nameSpace = e.getKey().getKey();
            String lockKey = e.getKey().getValue();
            String value = e.getValue();
            boolean b = unLockFallbackable(nameSpace, lockKey, value);
            if (!b) {
                log.arg0(nameSpace).arg1(lockKey).arg2(value).error("UNLOCK_FAILURE");
            }
            result = result && unLockFallbackable(nameSpace, lockKey, value);
        }
        return result;
    }

    abstract protected boolean tryLock(UK nameSpace, String lockKey, String encryptionKey, int milsToExpire);

    abstract protected boolean unLockFallbackable(UK nameSpace, String lockKey, String encryptionKey);

    @SneakyThrows(InterruptedException.class)
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
