package com.stellariver.milky.common.tool.exception;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultMessage {

    String value();

}
