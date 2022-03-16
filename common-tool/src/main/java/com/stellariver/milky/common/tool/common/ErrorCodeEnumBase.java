package com.stellariver.milky.common.tool.common;


import com.stellariver.milky.common.base.ErrorCode;

public class ErrorCodeEnumBase {

    public static final ErrorCode PARAM_IS_NULL = ErrorCode.code("PARAM_IS_NULL");

    public static final ErrorCode PARAM_FORMAT_IS_WRONG = ErrorCode.code("PARAM_FORMAT_IS_WRONG");

    public static final ErrorCode CONFIG_ERROR = ErrorCode.code("CONFIG_ERROR");

    public static final ErrorCode CONCURRENCY_VIOLATION = ErrorCode.code("CONCURRENCY_VIOLATION");

}
