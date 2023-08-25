package com.stellariver.milky.common.base;

import javax.validation.Payload;

public @interface Valids {

    String message() default "${internalMessage}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}