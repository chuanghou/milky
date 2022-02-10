package com.echobaba.milky.domain.support.depend;

import com.echobaba.milky.domain.support.command.Command;

public interface ConcurrentOperate {

    void sendOrderly(Command command);

    void receiveCommand(Command command);

}
