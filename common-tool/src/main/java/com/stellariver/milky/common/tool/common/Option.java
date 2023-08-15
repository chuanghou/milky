package com.stellariver.milky.common.tool.common;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
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

    List<Function<Object, String>> argsSelectors = new ArrayList<>();

    @Builder.Default
    @SuppressWarnings("unchecked")
    T defaultValue = (T) NULL_OBJECT;

    UK lambdaId;

    public boolean hasDefaultValue() {
        return defaultValue != NULL_OBJECT;
    }

}
