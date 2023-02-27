package com.stellariver.milky.demo.domain.item.event;

import com.stellariver.milky.common.tool.common.Clock;
import com.stellariver.milky.demo.basic.TypedEnums;
import com.stellariver.milky.demo.domain.item.Item;
import com.stellariver.milky.demo.domain.item.command.ItemTitleUpdateCommand;
import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.interceptor.Intercept;
import com.stellariver.milky.domain.support.interceptor.Interceptors;
import com.stellariver.milky.domain.support.interceptor.PosEnum;
import lombok.SneakyThrows;

import static com.stellariver.milky.demo.basic.TypedEnums.*;

public class ItemInterceptors implements Interceptors {

    @SneakyThrows
    @Intercept(pos = PosEnum.BEFORE)
    public void interceptBefore(ItemTitleUpdateCommand command, Item item, Context context) {
        context.addMetaData(MARK_BEFORE.class, Clock.currentTimeMillis());
        Thread.sleep(10);
    }


    @SneakyThrows
    @Intercept(pos = PosEnum.AFTER)
    public void interceptAfter(ItemTitleUpdateCommand command, Item item, Context context) {
        context.addMetaData(MARK_AFTER.class, Clock.currentTimeMillis());
    }

}
