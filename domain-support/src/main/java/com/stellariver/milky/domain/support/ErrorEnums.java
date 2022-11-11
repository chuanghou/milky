package com.stellariver.milky.domain.support;

import com.stellariver.milky.common.base.ErrorEnum;
import com.stellariver.milky.common.tool.exception.DefaultMessage;
import com.stellariver.milky.common.tool.exception.ErrorEnumsBase;
import com.stellariver.milky.common.tool.common.Kit;

import java.lang.reflect.Field;

public class ErrorEnums extends ErrorEnumsBase {

    public static ErrorEnum HANDLER_NOT_EXIST ;

    @DefaultMessage("聚合根不能继承")
    public static ErrorEnum AGGREGATE_INHERITED;

    @DefaultMessage("聚合根不存在")
    public static ErrorEnum AGGREGATE_NOT_EXISTED;

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
            String message = Kit.op(field.getAnnotation(DefaultMessage.class))
                    .map(DefaultMessage::value).orElse("系统繁忙请稍后再试");
            ErrorEnum errorEnum = new ErrorEnum(code, message, null);
            try {
                field.set(null, errorEnum);
            } catch (Throwable ignore) {}
        }
    }



}
