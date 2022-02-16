package com.stellariver.milky.example.domain.configuration;

import com.stellariver.milky.domain.support.depend.ConcurrentOperate;
import org.springframework.stereotype.Component;

@Component
public class ConcurrentOperateImpl implements ConcurrentOperate {

    @Override
    public boolean tryLock(String lockKey, int secondsToExpire) {
        return true;
    }

    @Override
    public boolean unlock(String lockKey) {
        return true;
    }

}
