package com.stellariver.milky.common.tool.common;


import com.stellariver.milky.common.base.ErrorCode;

public class ErrorCodeBase {

    public static final ErrorCode UNKNOWN = ErrorCode.code("UNKNOWN");

    public static final ErrorCode THIRD_SERVICE = ErrorCode.code("THIRD_SERVICE");

    public static final ErrorCode PARAM_IS_NULL = ErrorCode.code("PARAM_IS_NULL");

    public static final ErrorCode PARAM_IS_WRONG = ErrorCode.code("PARAM_IS_WRONG");

    public static final ErrorCode CONFIG_ERROR = ErrorCode.code("CONFIG_ERROR");

    public static final ErrorCode CONCURRENCY_VIOLATION = ErrorCode.code("CONCURRENCY_VIOLATION");

    static public ErrorCode withPrefix(Prefix prefix, String code, String message) {
        return ErrorCode.code(prefix.getPreFix() + "_" + code).message(message);
    }
}
