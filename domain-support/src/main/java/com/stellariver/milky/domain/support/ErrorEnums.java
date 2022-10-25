package com.stellariver.milky.domain.support;

import com.stellariver.milky.common.base.ErrorEnum;
import com.stellariver.milky.common.tool.exception.DefaultMessage;
import com.stellariver.milky.common.tool.exception.ErrorEnumsBase;
import com.stellariver.milky.common.tool.util.Kit;

import java.lang.reflect.Field;

public class ErrorEnums extends ErrorEnumsBase {

    public static ErrorEnum HANDLER_NOT_EXIST ;

    @DefaultMessage("aggregate couldn't be inherited!")
    public static ErrorEnum AGGREGATE_INHERITED;

    @DefaultMessage("aggregate couldn't be found!")
    public static ErrorEnum AGGREGATE_NOT_EXISTED;

    static {
        for (Field field : ErrorEnumsBase.class.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object o = field.get(null);
                if (o != null) {
                    continue;
                }
            } catch (Throwable ignore) {
            }
            String name = field.getName();
            ErrorEnum errorEnum = ErrorEnum.code(name).message(Kit.op(field.getAnnotation(DefaultMessage.class))
                    .map(DefaultMessage::value).orElse("系统繁忙请稍后再试"));
            try {
                field.set(null, errorEnum);
            } catch (Throwable ignore) {
            }
        }
    }



}
