package com.stellariver.milky.common.tool.common;


import com.stellariver.milky.common.base.ErrorCode;

public class ErrorCodeBase {

    public static final ErrorCode UNKNOWN = ErrorCode.code("UNKNOWN");

    public static final ErrorCode THIRD_SERVICE = ErrorCode.code("THIRD_SERVICE");

    public static final ErrorCode PARAM_IS_NULL = ErrorCode.code("PARAM_IS_NULL");

    public static final ErrorCode PARAM_IS_WRONG = ErrorCode.code("PARAM_IS_WRONG");

    static public ErrorCode withPrefix(Prefix prefix, String code) {
        return ErrorCode.code(prefix.getPreFix() + "_" + code);
    }

    static public ErrorCode thirdService(Prefix prefix, String code, String message) {
        return ErrorCode.code(prefix.getPreFix() + "_" + code).message(message);
    }
}
