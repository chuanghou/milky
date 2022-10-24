package com.stellariver.milky.demo.adapter.repository;

import com.stellariver.milky.common.tool.common.NameSpace;
import com.stellariver.milky.domain.support.command.Command;
import com.stellariver.milky.domain.support.dependency.ConcurrentOperate;
import org.springframework.stereotype.Component;

@Component
public class ConcurrentOperateImpl extends ConcurrentOperate {

    @Override
    public boolean tryLock(NameSpace nameSpace, String lockKey, String encryptionKey, int milsToExpire) {
        return true;
    }

    @Override
    public boolean unlock(NameSpace nameSpace, String lockKey, String encryptionKey) {
        return true;
    }
}
