package com.stellariver.milky.common.tool.common;


import com.stellariver.milky.common.base.ErrorEnum;

import java.lang.reflect.Field;

public class ErrorEnumsBase {

    public static ErrorEnum UNDEFINED;

    public static ErrorEnum MERGE_EXCEPTION;

    public static ErrorEnum NOT_ALLOW_LOST ;

    public static ErrorEnum NOT_SUPPORT_DIFFERENT_TYPE_COMPARE;

    public static ErrorEnum PARAM_FORMAT_WRONG;

    @DefaultMessage("参数为空")
    public static ErrorEnum PARAM_IS_NULL;

    @DefaultMessage("do对象成员不允许为NULL，请使用特殊值代替空语义")
    public static ErrorEnum FIELD_IS_NULL;

    public static ErrorEnum configErrorEnum;

    @DefaultMessage("并发操作失败")
    public static ErrorEnum CONCURRENCY_VIOLATION;

    public static ErrorEnum ENTITY_NOT_FOUND;

    public static ErrorEnum SYSTEM_EXCEPTION;


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
