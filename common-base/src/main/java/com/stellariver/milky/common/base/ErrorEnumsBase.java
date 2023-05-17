package com.stellariver.milky.common.base;


import java.lang.reflect.Field;
import java.util.Optional;

/**
 * @author houchuang
 */
public class ErrorEnumsBase {

    public static ErrorEnum MERGE_EX;

    public static ErrorEnum NOT_ALLOW_LOST;

    @Message("系统累瘫了请稍后再试")
    public static ErrorEnum OPTIMISTIC_COMPETITION;

    public static ErrorEnum PARAM_FORMAT_WRONG;

    public static ErrorEnum NOT_VALID;

    public static ErrorEnum CONFIG_ERROR;

    public static ErrorEnum NOT_VALID_NET_ADDRESS;

    public static ErrorEnum REPEAT_VALIDATE_GROUP;

    public static ErrorEnum MILKY_WIRED_FAILURE;

    public static ErrorEnum LOAD_NEXT_SECTION_LIMIT;

    @Message("重复命名空间")
    public static ErrorEnum DUPLICATE_NAME_SPACE;

    public static ErrorEnum ENTITY_NOT_FOUND;

    public static ErrorEnum SYS_EX;

    public static ErrorEnum RPC_EXCEPTION;

    public static ErrorEnum PERSISTENCE_ERROR;

    public static ErrorEnum UNREACHABLE_CODE;

    public static ErrorEnum DEEP_PAGING;

    @Message("当前流量太大，请稍后再试!")
    public static ErrorEnum FLOW_CONFIG;

    @Message("参数为空")
    public static ErrorEnum PARAM_IS_NULL;

    @Message("使用Milkywired字段$field$格式不正确")
    public static ErrorEnum FIELD_FORMAT_WRONG;

    @Message("并发操作失败")
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
            String message = Optional.ofNullable(field.getAnnotation(Message.class)).map(Message::value).filter(s -> !s.isEmpty()).orElse("系统累趴下啦！请稍后再试");
            String prefix = Optional.ofNullable(field.getAnnotation(Message.class)).map(Message::prefix).orElse("");
            String p = prefix.isEmpty() ? prefix : prefix + ": ";
            ErrorEnum errorEnum = new ErrorEnum(code, p + message);
            try {
                field.set(null, errorEnum);
            } catch (Throwable ignore) {}
        }
    }
}
