package com.stellariver.milky.common.tool.common;


import com.stellariver.milky.common.base.ErrorCode;

import java.util.function.Supplier;

public class ErrorCodeEnumBase {

    public static final ErrorCode UNDEFINED = ErrorCode.code("UNDEFINED");

    public static final ErrorCode PARAM_IS_NULL = ErrorCode.code("PARAM_IS_NULL");

    public static final ErrorCode PARAM_FORMAT_IS_WRONG = ErrorCode.code("PARAM_FORMAT_IS_WRONG");

    public static final ErrorCode CONFIG_ERROR = ErrorCode.code("CONFIG_ERROR");

    public static final ErrorCode CONCURRENCY_VIOLATION = ErrorCode.code("CONCURRENCY_VIOLATION");

    public static ErrorCode message(Supplier<String> supplier) {
        return ErrorCodeEnumBase.UNDEFINED.message(supplier.get());
    }

    public static ErrorCode message(String message) {
        return ErrorCodeEnumBase.UNDEFINED.message(message);
    }

}
