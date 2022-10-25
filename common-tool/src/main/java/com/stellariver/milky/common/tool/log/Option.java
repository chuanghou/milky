package com.stellariver.milky.common.tool.log;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.function.Function;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Option<R, T> {

    @Builder.Default
    boolean alwaysLog = false;

    @Builder.Default
    int retryTimes = 0;

    @Builder.Default
    Function<R, Boolean> check = r -> true;

    Function<R, T> transfer;

    Function<R, String> logResultSelector;

    T defaultValue;

    @Builder.Default
    int stackTraceLevel = 3;
}
