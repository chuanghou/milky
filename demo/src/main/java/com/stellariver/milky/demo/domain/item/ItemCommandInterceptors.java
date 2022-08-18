//package com.stellariver.milky.demo.domain.item;
//
//import com.stellariver.milky.demo.domain.item.command.ItemCreateCommand;
//import com.stellariver.milky.demo.domain.item.command.ItemTitleUpdateCommand;
//import com.stellariver.milky.domain.support.context.Context;
//import com.stellariver.milky.domain.support.interceptor.Intercept;
//import com.stellariver.milky.domain.support.interceptor.Interceptors;
//import com.stellariver.milky.domain.support.interceptor.PosEnum;
//
//public class ItemCommandInterceptors implements Interceptors {
//
//    @Intercept(pos = PosEnum.BEFORE)
//    public void changeOperator(ItemCreateCommand command, Context context) {
//    }
//
//    @Intercept(pos = PosEnum.AFTER)
//    public void changeOperator(ItemTitleUpdateCommand command, Context context) {
//    }
//}
