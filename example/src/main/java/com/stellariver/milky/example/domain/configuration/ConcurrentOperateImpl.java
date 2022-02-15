package com.stellariver.milky.example.domain.configuration;

import com.stellariver.milky.domain.support.command.Command;
import com.stellariver.milky.domain.support.depend.ConcurrentOperate;
import org.springframework.stereotype.Service;

@Service
public class ConcurrentOperateImpl implements ConcurrentOperate {
    @Override
    public void sendOrderly(Command command) {

    }

    @Override
    public void receiveCommand(Command command) {

    }

    @Override
    public boolean tryLock(String lockKey, int secondsToExpire) {
        return true;
    }

    @Override
    public boolean unlock(String lockKey) {
        return true;
    }
}
