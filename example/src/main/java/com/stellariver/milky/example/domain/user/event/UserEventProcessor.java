package com.stellariver.milky.example.domain.user.event;

import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.event.EventHandler;
import com.stellariver.milky.domain.support.event.EventProcessor;
import com.stellariver.milky.domain.support.event.HandlerTypeEnum;


public class UserEventProcessor implements EventProcessor {

    @EventHandler(order = 1, type = HandlerTypeEnum.ASYNC)
    public void handle1(NameChangeEvent event, Context context) {
        System.out.println(event);
    }

}
