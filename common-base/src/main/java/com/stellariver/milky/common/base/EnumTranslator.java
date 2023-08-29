package com.stellariver.milky.common.base;

public @interface EnumTranslator {

    Class<? extends Enum<?>> enumType();

    String field() default "desc";

}
