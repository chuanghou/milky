package com.stellariver.milky.domain.support;

import com.stellariver.milky.common.base.Error;
import com.stellariver.milky.common.tool.common.ErrorEnumBase;

public class ErrorEnum extends ErrorEnumBase {

    public static final Error CONTEXT_VALUE_PROVIDER_NOT_EXIST= Error.code("CONTEXT_VALUE_PROVIDER_NOT_EXIST");

    public static final Error HANDLER_NOT_EXIST= Error.code("HANDLER_NOT_EXIST");

    public static final Error AGGREGATE_INHERITED= Error.code("AGGREGATE_INHERITED").message("aggregate couldn't be inherited!");


    public static final Error AGGREGATE_NOT_EXISTED= Error.code("AGGREGATE_NOT_EXISTED").message("aggregate couldn't be found!");

}
