package com.stellariver.milky.demo.domain.item.dependency;

import com.stellariver.milky.demo.domain.item.command.ItemCreateCommand;
import com.stellariver.milky.domain.support.context.Context;

public class ItemDependencyPrepares {


    public UserInfo userInfo(ItemCreateCommand command, Context context) {
        return UserInfo.builder().userName("Tom").build();
    }
}
