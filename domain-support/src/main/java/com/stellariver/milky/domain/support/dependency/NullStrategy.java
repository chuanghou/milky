package com.stellariver.milky.domain.support.dependency;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public
enum NullStrategy {

    INTEGER_MIN_VALUE(Integer.MIN_VALUE),
    LONG_MIN_VALUE(Long.MIN_VALUE),
    STRING_NULL_STR("NULL"),
    STRING_BLANK(""),
    CUSTOM(null);

    @Getter
    final Object holderValue;

}
