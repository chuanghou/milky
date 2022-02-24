package com.stellariver.milky.example.domain.user.event;

import com.stellariver.milky.common.tool.utils.Json;
import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.event.EventHandler;
import com.stellariver.milky.domain.support.event.EventProcessor;
import com.stellariver.milky.domain.support.event.HandlerTypeEnum;
import com.stellariver.milky.example.infrastructure.mq.MqProducer;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class UserEventProcessor implements EventProcessor {

    final private MqProducer mqProducer;

    @EventHandler(order = 1, type = HandlerTypeEnum.ASYNC)
    public void handle1(NameChangeEvent event, Context context) {
        System.out.println(event);
    }


    @EventHandler(order = 1, type = HandlerTypeEnum.ASYNC)
    public void handle2(NameChangeEvent event, Context context) {
        System.out.println(event);
    }

    @EventHandler(order = 1, type = HandlerTypeEnum.ASYNC)
    public void handle3(NameChangeEvent event, Context context) {

        mqProducer.send("XXXTopic", Json.toString(event));

    }

}
