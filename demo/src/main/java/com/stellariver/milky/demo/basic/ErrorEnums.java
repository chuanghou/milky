package com.stellariver.milky.demo.basic;

import com.stellariver.milky.common.base.ErrorEnum;
import com.stellariver.milky.common.tool.exception.DefaultMessage;
import com.stellariver.milky.common.tool.exception.ErrorEnumsBase;
import com.stellariver.milky.common.tool.common.Kit;

import java.lang.reflect.Field;

/**
 * @author houchuang
 */
public class ErrorEnums extends ErrorEnumsBase {

    public static ErrorEnum ITEM_NOT_EXIST;

    public static ErrorEnum MOCK_EXCEPTION;

    static {
        for (Field field : ErrorEnums.class.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object o = field.get(null);
                if (o != null) {
                    continue;
                }
            } catch (Throwable ignore) {}
            String name = field.getName();
            String message = Kit.op(field.getAnnotation(DefaultMessage.class)).map(DefaultMessage::value).orElse("系统繁忙请稍后再试");
            ErrorEnum errorEnum = new ErrorEnum(name, message, null);
            try {
                field.set(null, errorEnum);
            } catch (Throwable ignore) {}
        }
    }

}
