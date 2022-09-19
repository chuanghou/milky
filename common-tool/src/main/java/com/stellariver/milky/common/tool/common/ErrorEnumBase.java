package com.stellariver.milky.common.tool.common;


import com.stellariver.milky.common.base.Error;

import java.util.function.Supplier;

public class ErrorEnumBase {

    public static final Error UNDEFINED = Error.code("UNDEFINED");

    public static final Error MERGE_EXCEPTION = Error.code("MERGE_EXCEPTION");

    public static final Error NOT_ALLOW_LOST = Error.code("NOT_ALLOW_LOST");

    public static final Error NOT_SUPPORT_DIFFERENT_TYPE_COMPARE = Error.code("NOT_SUPPORT_DIFFERENT_TYPE_COMPARE");

    public static final Error PARAM_FORMAT_WRONG = Error.code("PARAM_FORMAT_WRONG");

    public static final Error PARAM_IS_NULL = Error.code("PARAM_IS_NULL").message("参数为空");

    public static final Error FIELD_IS_NULL = Error.code("FIELD_IS_NULL").message("do对象成员不允许为NULL，请使用特殊值代替空语义");

    public static final Error CONFIG_ERROR = Error.code("CONFIG_ERROR").message("配置错误");

    public static final Error CONCURRENCY_VIOLATION = Error.code("CONCURRENCY_VIOLATION").message("并发操作失败");

    public static final Error ENTITY_NOT_FOUND = Error.code("ENTITY_NOT_FOUND");

    public static final Error SYSTEM_EXCEPTION = Error.code("SYSTEM_EXCEPTION");


}
