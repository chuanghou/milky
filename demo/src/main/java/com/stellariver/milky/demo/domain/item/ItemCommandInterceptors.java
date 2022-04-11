package com.stellariver.milky.demo.domain.item;

import com.stellariver.milky.demo.domain.item.command.ItemCreateCommand;
import com.stellariver.milky.demo.domain.item.command.ItemUpdateCommand;
import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.interceptor.BusInterceptor;
import com.stellariver.milky.domain.support.interceptor.BusInterceptors;
import com.stellariver.milky.domain.support.interceptor.PosEnum;

public class ItemCommandInterceptors implements BusInterceptors {

    @BusInterceptor(pos = PosEnum.BEFORE)
    public void changeOperator(ItemCreateCommand command, Context context) {
        context.putMetaData("operatorSource" , "tom");
    }

    @BusInterceptor(pos = PosEnum.AFTER)
    public void changeOperator(ItemUpdateCommand command, Context context) {
        context.putMetaData("operatorSource" , "tom");
    }
}
