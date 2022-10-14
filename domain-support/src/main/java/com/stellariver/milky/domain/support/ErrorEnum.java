package com.stellariver.milky.domain.support;

import com.stellariver.milky.common.base.Error;
import com.stellariver.milky.common.tool.common.DefaultMessage;
import com.stellariver.milky.common.tool.common.ErrorEnumBase;
import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.common.SysException;

import java.util.Arrays;

public class ErrorEnum extends ErrorEnumBase {

    public static final Error HANDLER_NOT_EXIST= Error.code("HANDLER_NOT_EXIST");

    public static final Error AGGREGATE_INHERITED= Error.code("AGGREGATE_INHERITED").message("aggregate couldn't be inherited!");

    public static final Error AGGREGATE_NOT_EXISTED= Error.code("AGGREGATE_NOT_EXISTED").message("aggregate couldn't be found!");

    static {
        Arrays.stream(ErrorEnumBase.class.getDeclaredFields()).forEach(field -> {
            field.setAccessible(true);
            String name = field.getName();
            String message = Kit.op(field.getAnnotation(DefaultMessage.class)).map(DefaultMessage::value).orElse("");
            Error error = Error.code(name).message(message);
            try {
                field.set(null, error);
            } catch (IllegalAccessException e) {
                throw new SysException(e);
            }
        });
    }

}
