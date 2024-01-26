package com.stellariver.milky.demo.infrastructure.redis;

import com.stellariver.milky.common.base.ErrorEnumsBase;
import com.stellariver.milky.common.base.ExceptionType;
import com.stellariver.milky.common.base.Result;
import com.stellariver.milky.domain.support.dependency.ConcurrentOperate;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author houchuang
 */
@CustomLog
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConcurrentOperateImpl extends ConcurrentOperate {

    private final Map<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    @Override
    @SneakyThrows
    protected Map<String, Result<Void>> batchTryLock(List<Pair<String, Duration>> lockParams) {
        Map<String, Result<Void>> resultMap = new HashMap<>();
        for (Pair<String, Duration> lockParam : lockParams) {
            String key = lockParam.getLeft();
            ReentrantLock reentrantLock = lockMap.computeIfAbsent(key, k -> new ReentrantLock());
            if (reentrantLock.tryLock(3, TimeUnit.SECONDS)) {
                resultMap.put(key, Result.success());
            } else {
                log.arg0(lockParam).error("LOCK_FAIL");
                resultMap.put(key, Result.error(ErrorEnumsBase.CONCURRENCY_VIOLATION, ExceptionType.BIZ));
            }
        }
        return resultMap;
    }

    @Override
    protected Map<String, Result<Void>> batchUnLock(Set<String> unlockIds) {
        Map<String, Result<Void>> resultMap = new HashMap<>();
        unlockIds.forEach(key -> {
            ReentrantLock reentrantLock = lockMap.get(key);
            reentrantLock.unlock();
            resultMap.put(key, Result.success());
        });
        return resultMap;
    }
}
