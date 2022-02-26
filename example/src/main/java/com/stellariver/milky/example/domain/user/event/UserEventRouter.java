package com.stellariver.milky.example.domain.user.event;

import com.stellariver.milky.common.tool.utils.Json;
import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.event.Router;
import com.stellariver.milky.domain.support.event.EventRouter;
import com.stellariver.milky.domain.support.event.TypeEnum;
import com.stellariver.milky.example.infrastructure.mq.MqProducer;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class UserEventRouter implements EventRouter {

    final private MqProducer mqProducer;

    @Router(order = 1, type = TypeEnum.ASYNC)
    public void handle1(NameChangeEvent event, Context context) {
        System.out.println(event);
    }


    @Router(order = 1, type = TypeEnum.ASYNC)
    public void handle2(NameChangeEvent event, Context context) {
        System.out.println(event);
    }

    @Router(order = 1, type = TypeEnum.ASYNC)
    public void handle3(NameChangeEvent event, Context context) {

        mqProducer.send("XXXTopic", Json.toString(event));

    }

}
