package com.stellariver.milky.domain.support.dependency;

import com.stellariver.milky.common.base.Result;
import com.stellariver.milky.common.tool.exception.SysException;
import com.stellariver.milky.common.tool.common.UK;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.domain.support.base.RetryParameter;
import lombok.CustomLog;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * @author houchuang
 */
@CustomLog
public abstract class ConcurrentOperate {

    private final ThreadLocal<Set<String>> lockedIds = ThreadLocal.withInitial(HashSet::new);

    public boolean tryReentrantLock(UK nameSpace, String lockKey, int milsToExpire) {
        String lockId = nameSpace.preFix(lockKey);
        boolean contains = lockedIds.get().contains(lockId);
        if (!contains) {
            boolean locked = tryLock(nameSpace.preFix(lockKey), milsToExpire);
            if (!locked) {
                return false;
            }
            lockedIds.get().add(lockId);
        }
        return true;
    }

    public boolean unLockAll() {
        Map<String, Result<Void>> unLockResult = batchUnLock(lockedIds.get());
        return unLockResult.values().stream().map(Result::getSuccess).reduce((b0, b1) -> b0 && b1).orElse(false);
    }

    protected boolean tryLock(String lockId, int milsToExpire) {
        Pair<String, Duration> lockParam = Pair.of(lockId, Duration.of(milsToExpire, ChronoUnit.MILLIS));
        Map<String, Result<Void>> lockResult = batchTryLock(Collect.asList(lockParam));
        Result<?> result = lockResult.get(lockId);
        return result.getSuccess();
    }

    abstract protected Map<String, Result<Void>> batchTryLock(List<Pair<String, Duration>> lockParams);

    abstract protected Map<String, Result<Void>> batchUnLock(Set<String> unlockIds);

    @SneakyThrows(InterruptedException.class)
    public boolean tryRetryLock(RetryParameter retryParameter) {
        SysException.anyNullThrow(retryParameter.getLockKey());
        SysException.trueThrow(retryParameter.getTimes() <= 0, "retry times should not smaller than 0 or equal with 0");
        SysException.trueThrow(retryParameter.getSleepTimeMils() > 5000, "sleep time is too long");
        int times = retryParameter.getTimes();
        String lockKey = retryParameter.getLockKey();
        int milsToExpire = retryParameter.getMilsToExpire();
        while (times-- > 0) {
            Thread.sleep(retryParameter.getSleepTimeMils());
            if (tryReentrantLock(retryParameter.getNameSpace(), lockKey, milsToExpire)) {
                return true;
            }
        }
        return false;
    }


}
