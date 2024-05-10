package com.stellariver.milky.domain.support.base;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NotExistedMessage {

    String value();

}
