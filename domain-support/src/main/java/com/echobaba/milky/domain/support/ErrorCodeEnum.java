package com.echobaba.milky.domain.support;

import com.echobaba.milky.client.base.ErrorCode;

public class ErrorCodeEnum extends ErrorCode {

    public static final ErrorCode CONFIG_ERROR= code("CONFIG_ERROR").build();

    public static final ErrorCode CONCURRENT_OPERATE_LOCK= code("CONCURRENT_OPERATE_LOCK").message("并发操作冲突").build();

    public static final ErrorCode CONTEXT_VALUE_PROVIDER_NOT_EXIST= code("CONTEXT_VALUE_PROVIDER_NOT_EXIST").build();

    public static final ErrorCode HANDLER_NOT_EXIST= code("HANDLER_NOT_EXIST").build();

}
