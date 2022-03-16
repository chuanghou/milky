package com.stellariver.milky.example.domain.user.event;

import com.stellariver.milky.common.tool.utils.Json;
import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.event.EventRouter;
import com.stellariver.milky.domain.support.event.EventRouters;
import com.stellariver.milky.domain.support.event.TypeEnum;
import com.stellariver.milky.example.infrastructure.mq.MqProducer;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class UserEventRouters implements EventRouters {

    final private MqProducer mqProducer;

    @EventRouter
    public void route1(NameChangeEvent event, Context context) {
        System.out.println(event);
    }


    @EventRouter(type = TypeEnum.ASYNC)
    public void route2(NameChangeEvent event, Context context) {
        System.out.println(event);
    }

    @EventRouter(type = TypeEnum.ASYNC)
    public void route3(NameChangeEvent event, Context context) {

        mqProducer.send("XXXTopic", Json.toString(event));

    }

}
