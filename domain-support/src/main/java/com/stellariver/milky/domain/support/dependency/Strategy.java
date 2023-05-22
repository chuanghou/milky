package com.stellariver.milky.domain.support.dependency;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Strategy {

    INTEGER_MIN_VALUE(Integer.MIN_VALUE),
    LONG_MIN_VALUE(Long.MIN_VALUE),
    INTEGER_MINUS_ONE(-1),
    LONG_MINUS_ONE(-1L),
    STRING_NULL_STR("NULL"),
    STRING_BLANK(""),
    IGNORE("There are some fields which won't be persisted, so the null value of these field won't be care!"),
    CUSTOM(null);

    @Getter
    final Object replacer;

}
