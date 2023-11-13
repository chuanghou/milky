package com.stellariver.milky.common.tool.common;

import com.stellariver.milky.common.base.BaseEx;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author houchuang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unchecked")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Option<R, T> {

    static final private Object NULL_OBJECT = new Object();

    @Builder.Default
    boolean alwaysLog = false;

    @Builder.Default
    int retryTimes = 0;

    @Builder.Default
    BiFunction<R, Throwable, Boolean> retryable = (r, t) -> false;

    @Builder.Default
    Function<R, ? extends BaseEx> checker = r -> null;

    @Builder.Default
    T defaultValue = (T) NULL_OBJECT;

    @Builder.Default
    Function<R, T> transfer = r -> (T) r;

    @Builder.Default
    Function<R, String> resultPrinter = Objects::toString;

    UK lambdaId;

    public boolean hasDefaultValue() {
        return defaultValue != NULL_OBJECT;
    }

}
