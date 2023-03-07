package com.stellariver.milky.common.tool.exception;


import com.stellariver.milky.common.base.ErrorEnum;
import com.stellariver.milky.common.tool.common.Kit;

import java.lang.reflect.Field;

/**
 * @author houchuang
 */
public class ErrorEnumsBase {

    public static ErrorEnum MERGE_EXCEPTION;

    public static ErrorEnum NOT_ALLOW_LOST;

    @Message("系统累瘫了请稍后再试")
    public static ErrorEnum OPTIMISTIC_COMPETITION;

    public static ErrorEnum PARAM_FORMAT_WRONG;

    public static ErrorEnum CONFIG_ERROR;

    public static ErrorEnum MILKY_WIRED_FAILURE;

    public static ErrorEnum LOAD_NEXT_SECTION_LIMIT;

    @Message("重复命名空间")
    public static ErrorEnum DUPLICATE_NAME_SPACE;

    public static ErrorEnum ENTITY_NOT_FOUND;

    public static ErrorEnum SYSTEM_EXCEPTION;

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
            } catch (Throwable ignore) {
            }
            String code = field.getName();
            String message = Kit.op(field.getAnnotation(Message.class)).map(Message::value).orElse("系统繁忙请稍后再试");
            ErrorEnum errorEnum = new ErrorEnum(code, message, null);
            try {
                field.set(null, errorEnum);
            } catch (Throwable ignore) {
            }
        }
    }
}
