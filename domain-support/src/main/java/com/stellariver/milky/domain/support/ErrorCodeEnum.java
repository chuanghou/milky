package com.stellariver.milky.domain.support;

import com.stellariver.milky.client.base.ErrorCode;
import com.stellariver.milky.common.tool.common.ErrorCodeBase;

public class ErrorCodeEnum extends ErrorCodeBase {

    public static final ErrorCode CONFIG_ERROR = ErrorCode.code("CONFIG_ERROR");

    public static final ErrorCode CONCURRENT_OPERATE_LOCK= ErrorCode.code("CONCURRENT_OPERATE_LOCK").message("并发操作冲突");

    public static final ErrorCode CONTEXT_VALUE_PROVIDER_NOT_EXIST= ErrorCode.code("CONTEXT_VALUE_PROVIDER_NOT_EXIST");

    public static final ErrorCode HANDLER_NOT_EXIST= ErrorCode.code("HANDLER_NOT_EXIST");

}
