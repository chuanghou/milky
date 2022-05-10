package com.stellariver.milky.common.tool.common;


import com.stellariver.milky.common.base.Error;

import java.util.function.Supplier;

public class ErrorEnumBase {

    public static final Error UNDEFINED = Error.code("UNDEFINED");

    public static final Error PARAM_IS_NULL = Error.code("PARAM_IS_NULL").message("参数为空");

    public static final Error PARAM_FORMAT_IS_WRONG = Error.code("PARAM_FORMAT_IS_WRONG").message("参数格式不正确");

    public static final Error CONFIG_ERROR = Error.code("CONFIG_ERROR").message("配置错误");

    public static final Error FLOW_CONTROL = Error.code("FLOW_CONTROL").message("流控");

    public static final Error META_DATA_DUPLICATE_KEY = Error.code("META_DATA_DUPLICATE_KEY");

    public static final Error CONCURRENCY_VIOLATION = Error.code("CONCURRENCY_VIOLATION").message("并发操作失败");

    public static final Error ENTITY_NOT_FOUND = Error.code("ENTITY_NOT_FOUND");

    public static Error message(Supplier<String> supplier) {
        return ErrorEnumBase.UNDEFINED.message(supplier.get());
    }

    public static Error message(String message) {
        return ErrorEnumBase.UNDEFINED.message(message);
    }

}
