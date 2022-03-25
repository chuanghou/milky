package com.stellariver.milky.common.tool.common;

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
    boolean withLog = false;

    @Builder.Default
    int retryTimes = 0;

    Function<R, Boolean> check;

    Function<R, T> transfer;

    T defaultValue;
}
