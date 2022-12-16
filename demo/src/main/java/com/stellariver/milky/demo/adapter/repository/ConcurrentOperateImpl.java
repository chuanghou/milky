package com.stellariver.milky.demo.adapter.repository;

import com.stellariver.milky.common.tool.common.UK;
import com.stellariver.milky.domain.support.dependency.ConcurrentOperate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConcurrentOperateImpl extends ConcurrentOperate {

//    final RedisHelper redisHelper;

    @Override
    public boolean tryLock(UK nameSpace, String lockKey, String encryptionKey, int milsToExpire) {
//        return redisHelper.tryLock(nameSpace, lockKey, encryptionKey, milsToExpire);
        return true;
    }

    @Override
    public boolean unLockFallbackable(UK nameSpace, String lockKey, String encryptionKey) {
//        return redisHelper.unlockFallbackable(nameSpace, lockKey, encryptionKey, true);
        return true;
    }

}
