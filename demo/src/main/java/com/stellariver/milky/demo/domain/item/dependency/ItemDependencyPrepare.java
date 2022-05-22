package com.stellariver.milky.demo.domain.item.dependency;

import com.stellariver.milky.demo.domain.item.command.ItemCreateCommand;
import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.context.DependencyKey;
import com.stellariver.milky.domain.support.context.DependencyPrepares;

public class ItemDependencyPrepare implements DependencyPrepares {

    @DependencyKey("userName")
    public void prepare(ItemCreateCommand command, Context context) {

    }
}
