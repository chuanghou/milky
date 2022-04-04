package com.stellariver.milky.demo.adapter.repository;

import com.stellariver.milky.domain.support.command.Command;
import com.stellariver.milky.domain.support.dependency.ConcurrentOperate;
import org.springframework.stereotype.Component;

@Component
public class ConcurrentOperateImpl implements ConcurrentOperate {
    @Override
    public void sendOrderly(Command command) {

    }

    @Override
    public void receiveCommand(Command command) {

    }

    @Override
    public boolean tryLock(String lockKey, String encryptionKey, int milsToExpire) {
        return true;
    }

    @Override
    public boolean unlock(String lockKey, String encryptionKey) {
        return true;
    }
}
