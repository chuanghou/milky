package com.stellariver.milky.demo.basic;

import com.stellariver.milky.common.base.Error;
import com.stellariver.milky.common.tool.common.DefaultMessage;
import com.stellariver.milky.common.tool.common.ErrorEnumBase;
import com.stellariver.milky.common.tool.common.Kit;

import java.lang.reflect.Field;

public class ErrorEnum extends ErrorEnumBase {

    public static Error ITEM_NOT_EXIST;

    public static Error MOCK_EXCEPTION;

    static {
        for (Field field : ErrorEnum.class.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object o = field.get(null);
                if (o != null) {
                    continue;
                }
            } catch (Throwable ignore) {
            }
            String name = field.getName();
            Error error = Error.code(name).message(Kit.op(field.getAnnotation(DefaultMessage.class))
                    .map(DefaultMessage::value).orElse("系统繁忙请稍后再试"));
            try {
                field.set(null, error);
            } catch (Throwable ignore) {
            }
        }
    }

}
