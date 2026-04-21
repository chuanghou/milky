package com.stellariver.milky.infrastructure.base.database;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Builder;
import lombok.Getter;

import java.util.function.Function;
import java.util.function.Supplier;

@Getter
@Builder
public class CursorOptions<T> {

    private final Supplier<LambdaQueryWrapper<T>> supplier;

    @Builder.Default
    private int batchSize = 1000;

    @Builder.Default
    private String cursorColumn = "id";

    private Function<T, Long> idGetter;

}
