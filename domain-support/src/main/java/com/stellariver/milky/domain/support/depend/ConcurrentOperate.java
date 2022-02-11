package com.stellariver.milky.domain.support.depend;

import com.stellariver.milky.domain.support.command.Command;

public interface ConcurrentOperate {

    void sendOrderly(Command command);

    void receiveCommand(Command command);

}
