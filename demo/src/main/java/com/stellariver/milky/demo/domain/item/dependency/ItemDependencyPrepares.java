package com.stellariver.milky.demo.domain.item.dependency;

import com.stellariver.milky.demo.basic.TypedEnums;
import com.stellariver.milky.demo.domain.item.command.ItemCreateCommand;
import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.context.DependencyKey;
import com.stellariver.milky.domain.support.context.DependencyPrepares;

import com.stellariver.milky.demo.basic.TypedEnums.*;

public class ItemDependencyPrepares implements DependencyPrepares {


    @DependencyKey(value = USER_INFO.class)
    public UserInfo userInfo(ItemCreateCommand command, Context context) {
        return UserInfo.builder().userName("Tom").build();
    }
}
