package com.echobaba.milky.domain.support;

import com.echobaba.milky.client.base.ErrorCode;
import com.echobaba.milky.common.tool.common.ErrorCodeBase;

public class ErrorCodeEnum extends ErrorCodeBase {

    public static final ErrorCode CONFIG_ERROR= ErrorCode.code("CONFIG_ERROR").build();

    public static final ErrorCode CONCURRENT_OPERATE_LOCK= ErrorCode.code("CONCURRENT_OPERATE_LOCK").message("并发操作冲突").build();

    public static final ErrorCode CONTEXT_VALUE_PROVIDER_NOT_EXIST= ErrorCode.code("CONTEXT_VALUE_PROVIDER_NOT_EXIST").build();

    public static final ErrorCode HANDLER_NOT_EXIST= ErrorCode.code("HANDLER_NOT_EXIST").build();

}
