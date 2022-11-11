package com.stellariver.milky.common.tool.exception;


import com.stellariver.milky.common.base.ErrorEnum;
import com.stellariver.milky.common.tool.common.Kit;

import java.lang.reflect.Field;

public class ErrorEnumsBase {

    public static ErrorEnum MERGE_EXCEPTION;

    public static ErrorEnum NOT_ALLOW_LOST ;

    public static ErrorEnum NOT_SUPPORT_DIFFERENT_TYPE_COMPARE;

    public static ErrorEnum PARAM_FORMAT_WRONG;

    public static ErrorEnum CONFIG_ERROR;

    public static ErrorEnum ENTITY_NOT_FOUND;

    public static ErrorEnum SYSTEM_EXCEPTION;

    public static ErrorEnum RPC_EXCEPTION;

    public static ErrorEnum NOT_REACHED_PART;

    @DefaultMessage("当前流量太大，请稍后再试!")
    public static ErrorEnum FLOW_CONFIG;

    @DefaultMessage("参数为空")
    public static ErrorEnum PARAM_IS_NULL;

    @DefaultMessage("DO对象成员不允许为NULL，请使用特殊值代替空语义")
    public static ErrorEnum FIELD_IS_NULL;

    @DefaultMessage("并发操作失败")
    public static ErrorEnum CONCURRENCY_VIOLATION;

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
            String message = Kit.op(field.getAnnotation(DefaultMessage.class)).map(DefaultMessage::value).orElse("系统繁忙请稍后再试");
            ErrorEnum errorEnum = new ErrorEnum(code, message, null);
            try {
                field.set(null, errorEnum);
            } catch (Throwable ignore) {}
        }
    }
}
