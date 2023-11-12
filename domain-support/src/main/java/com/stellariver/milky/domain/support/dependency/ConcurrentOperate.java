package com.stellariver.milky.domain.support.dependency;

import com.stellariver.milky.common.base.ExceptionType;
import com.stellariver.milky.common.base.Result;
import com.stellariver.milky.common.base.SysEx;
import com.stellariver.milky.common.tool.common.UK;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.domain.support.ErrorEnums;
import com.stellariver.milky.domain.support.base.RetryParameter;
import lombok.CustomLog;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

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

    public Pair<Boolean, Map<String, Result<Void>>> unLockAll() {
        Set<String> lockedKey = lockedIds.get();
        lockedIds.remove();
        Map<String, Result<Void>> unLockResult = batchUnLockWrapper(lockedKey);
        Boolean unlocked = unLockResult.values().stream().map(Result::getSuccess).reduce(true, Boolean::logicalAnd);
        return Pair.of(unlocked, unLockResult);
    }

    protected boolean tryLock(String lockId, int milsToExpire) {
        Pair<String, Duration> lockParam = Pair.of(lockId, Duration.of(milsToExpire, ChronoUnit.MILLIS));
        Map<String, Result<Void>> lockResult = batchTryLock(Collect.asList(lockParam));
        Result<?> result = lockResult.get(lockId);
        return result.getSuccess();
    }

    abstract protected Map<String, Result<Void>> batchTryLock(List<Pair<String, Duration>> lockParams);

    private Map<String, Result<Void>> batchUnLockWrapper(Set<String> unlockIds) {
        try {
            return batchUnLock(unlockIds);
        } catch (Throwable throwable) {
            log.position("batchUnLockWrapper").error(throwable.getMessage(), throwable);
        }
        return Collect.toMap(unlockIds, Function.identity(), unlockId -> Result.error(ErrorEnums.SYS_EX, ExceptionType.SYS));
    }
    abstract protected Map<String, Result<Void>> batchUnLock(Set<String> unlockIds);

    @SneakyThrows(InterruptedException.class)
    public boolean tryRetryLock(RetryParameter retryParameter) {
        SysEx.anyNullThrow(retryParameter.getLockKey());
        SysEx.trueThrow(retryParameter.getTimes() <= 0, "retry times should not smaller than 0 or equal with 0");
        SysEx.trueThrow(retryParameter.getSleepTimeMils() > 5000, "sleep time is too long");
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
