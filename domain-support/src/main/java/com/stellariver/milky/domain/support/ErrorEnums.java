package com.stellariver.milky.domain.support;

import com.stellariver.milky.common.base.ErrorEnum;
import com.stellariver.milky.common.tool.exception.Message;
import com.stellariver.milky.common.tool.exception.ErrorEnumsBase;
import com.stellariver.milky.common.tool.common.Kit;

import java.lang.reflect.Field;

/**
 * @author houchuang
 */
public class ErrorEnums extends ErrorEnumsBase {

    public static ErrorEnum HANDLER_NOT_EXIST ;

    @Message("聚合根不能继承")
    public static ErrorEnum AGGREGATE_INHERITED;

    @Message("聚合根不存在")
    public static ErrorEnum AGGREGATE_NOT_EXISTED;

    public static ErrorEnum REPEAT_DEPENDENCY_KEY;


    static {
        for (Field field : ErrorEnums.class.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object o = field.get(null);
                if (o != null) {
                    continue;
                }
            } catch (Throwable ignore) {}
            String code = field.getName();
            String message = Kit.op(field.getAnnotation(Message.class))
                    .map(Message::value).orElse("系统繁忙请稍后再试");
            ErrorEnum errorEnum = new ErrorEnum(code, message, null);
            try {
                field.set(null, errorEnum);
            } catch (Throwable ignore) {}
        }
    }



}
