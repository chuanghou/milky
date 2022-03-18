package com.stellariver.milky.domain.support.interceptor;

import com.stellariver.milky.domain.support.command.Command;
import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.event.Event;

public class UserBusInterceptors implements BusInterceptors {

    @BusInterceptor(pos = PosEnum.BEFORE)
    public void beforeCommandBus(Command command, Context context) {

    }

    @BusInterceptor(pos = PosEnum.AFTER)
    public void check(Command command, Context context) {

    }

    @BusInterceptor(pos = PosEnum.BEFORE)
    public void beforeEventBus(Event event, Context context) {

    }

    @BusInterceptor(pos = PosEnum.AFTER)
    public void afterEventBus(Event event, Context context) {

    }

}
