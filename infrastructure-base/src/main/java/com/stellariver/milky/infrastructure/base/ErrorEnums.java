package com.stellariver.milky.infrastructure.base;

import com.stellariver.milky.common.base.ErrorEnum;
import com.stellariver.milky.common.tool.exception.Message;
import com.stellariver.milky.common.tool.exception.ErrorEnumsBase;
import com.stellariver.milky.common.tool.common.Kit;

import java.lang.reflect.Field;

/**
 * @author houchuang
 */
public class ErrorEnums extends ErrorEnumsBase {

    public static ErrorEnum MESSAGE_RETRY;

    static {
        for (Field field : ErrorEnumsBase.class.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object o = field.get(null);
                if (o != null) {
                    continue;
                }
            } catch (Throwable ignore) {}

            String code = field.getName();
            String message = Kit.op(field.getAnnotation(Message.class)).map(Message::value).filter(s -> !s.isEmpty()).orElse("系统累趴下啦！请稍后再试");
            String prefix = Kit.op(field.getAnnotation(Message.class)).map(Message::prefix).orElse("");
            String p = prefix.isEmpty() ? prefix : prefix + ": ";
            ErrorEnum errorEnum = new ErrorEnum(code, p + message, null);
            try {
                field.set(null, errorEnum);
            } catch (Throwable ignore) {}
        }
    }

}
