package com.stellariver.milky.common.tool.common;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.function.Function;

/**
 * @author houchuang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Option<R, T> {

    static final private Object NULL_OBJECT = new Object();

    @Builder.Default
    boolean alwaysLog = false;

    @Builder.Default
    int retryTimes = 0;

    @Builder.Default
    Function<R, Boolean> check = r -> true;

    Function<R, T> transfer;

    Function<R, String> rSelector;

    @Builder.Default
    @SuppressWarnings("all")
    T defaultValue = (T) NULL_OBJECT;

    UK lambdaId;

    public boolean hasDefaultValue() {
        return defaultValue != NULL_OBJECT;
    }

}
