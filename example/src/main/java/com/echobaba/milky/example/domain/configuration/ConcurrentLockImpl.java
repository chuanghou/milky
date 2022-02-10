package com.echobaba.milky.example.domain.configuration;

import com.echobaba.milky.domain.support.depend.ConcurrentLock;
import org.springframework.stereotype.Service;

@Service
public class ConcurrentLockImpl implements ConcurrentLock {
    @Override
    public boolean tryLock(String lockKey, int secondsToExpire) {
        return false;
    }

    @Override
    public boolean unlock(String lockKey) {
        return false;
    }
}
