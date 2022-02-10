package com.echobaba.milky.example.domain.configuration;

import com.echobaba.milky.domain.support.command.Command;
import com.echobaba.milky.domain.support.depend.ConcurrentOperate;
import org.springframework.stereotype.Service;

@Service
public class ConcurrentOperateImpl implements ConcurrentOperate {
    @Override
    public void sendOrderly(Command command) {

    }

    @Override
    public void receiveCommand(Command command) {

    }
}
