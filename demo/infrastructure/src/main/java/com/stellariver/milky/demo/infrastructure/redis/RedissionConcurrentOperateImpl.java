package com.stellariver.milky.demo.infrastructure.redis;

import com.stellariver.milky.common.base.Result;
import com.stellariver.milky.domain.support.dependency.ConcurrentOperate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.tuple.Pair;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author houchuang
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RedissionConcurrentOperateImpl extends ConcurrentOperate {

    final RedissonClient redissonClient;

    @Override
    @SneakyThrows
    protected Map<String, Result<Void>> batchTryLock(List<Pair<String, Duration>> lockParams) {
        return lockParams.stream().collect(Collectors.toMap(Pair::getKey, p -> Result.success()));
    }

    @Override
    protected Map<String, Result<Void>> batchUnLock(Set<String> unlockIds) {
        return unlockIds.stream().collect(Collectors.toMap(Function.identity(), lockId -> Result.success()));
    }

}
