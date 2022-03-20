package com.stellariver.milky.common.tool.common;


import com.stellariver.milky.common.base.Code;

import java.util.function.Supplier;

public class CodeEnumBase {

    public static final Code UNDEFINED = Code.code("UNDEFINED");

    public static final Code PARAM_IS_NULL = Code.code("PARAM_IS_NULL");

    public static final Code PARAM_FORMAT_IS_WRONG = Code.code("PARAM_FORMAT_IS_WRONG");

    public static final Code CONFIG_ERROR = Code.code("CONFIG_ERROR");

    public static final Code CONCURRENCY_VIOLATION = Code.code("CONCURRENCY_VIOLATION");

    public static Code message(Supplier<String> supplier) {
        return CodeEnumBase.UNDEFINED.message(supplier.get());
    }

    public static Code message(String message) {
        return CodeEnumBase.UNDEFINED.message(message);
    }

}
