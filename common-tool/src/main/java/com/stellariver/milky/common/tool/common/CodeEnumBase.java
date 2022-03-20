package com.stellariver.milky.common.tool.common;


import com.stellariver.milky.common.base.Code;

import java.util.function.Supplier;

public class CodeEnumBase {

    public static final Code UNDEFINED = Code.code("UNDEFINED");

    public static final Code PARAM_IS_NULL = Code.code("PARAM_IS_NULL").message("参数为空");

    public static final Code PARAM_FORMAT_IS_WRONG = Code.code("PARAM_FORMAT_IS_WRONG").message("参数格式不正确");

    public static final Code CONFIG_ERROR = Code.code("CONFIG_ERROR").message("配置错误");

    public static final Code CONCURRENCY_VIOLATION = Code.code("CONCURRENCY_VIOLATION").message("并发操作失败");

    public static Code message(Supplier<String> supplier) {
        return CodeEnumBase.UNDEFINED.message(supplier.get());
    }

    public static Code message(String message) {
        return CodeEnumBase.UNDEFINED.message(message);
    }

}
