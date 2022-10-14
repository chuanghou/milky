package com.stellariver.milky.common.tool.common;


import com.stellariver.milky.common.base.Error;

import java.util.Arrays;

public class ErrorEnumBase {

    public static Error UNDEFINED;

    public static Error MERGE_EXCEPTION;

    public static Error NOT_ALLOW_LOST;

    public static Error NOT_SUPPORT_DIFFERENT_TYPE_COMPARE;

    public static Error PARAM_FORMAT_WRONG;

    @DefaultMessage("参数为空")
    public static Error PARAM_IS_NULL;

    @DefaultMessage("do对象成员不允许为NULL，请使用特殊值代替空语义")
    public static Error FIELD_IS_NULL;

    @DefaultMessage("配置错误")
    public static Error CONFIG_ERROR;

    @DefaultMessage("并发操作失败")
    public static Error CONCURRENCY_VIOLATION;

    public static Error ENTITY_NOT_FOUND;

    public static Error SYSTEM_EXCEPTION;

    static {
        Arrays.stream(ErrorEnumBase.class.getDeclaredFields()).forEach(field -> {
            field.setAccessible(true);
            String name = field.getName();
            String message = Kit.op(field.getAnnotation(DefaultMessage.class)).map(DefaultMessage::value).orElse("");
            Error error = Error.code(name).message(message);
            try {
                field.set(null, error);
            } catch (IllegalAccessException e) {
                throw new SysException(e);
            }
        });
    }

}
