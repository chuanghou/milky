package com.stellariver.milky.demo.adapter.repository;

import com.stellariver.milky.common.tool.common.UK;
import com.stellariver.milky.domain.support.dependency.ConcurrentOperate;
import org.springframework.stereotype.Component;

@Component
public class ConcurrentOperateImpl extends ConcurrentOperate {

    @Override
    public boolean tryLock(UK nameSpace, String lockKey, String encryptionKey, int milsToExpire) {
        return true;
    }

    @Override
    public boolean unlock(UK nameSpace, String lockKey, String encryptionKey) {
        return true;
    }
}
