package com.stellariver.milky.infrastructure.base.database;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Getter;

import java.util.function.Function;
import java.util.function.Supplier;

@Getter
public class CursorOptions<T> {

    private final Supplier<LambdaQueryWrapper<T>> supplier;
    private final Function<T, Long> idGetter;
    private int batchSize;
    private String cursorColumn;

    private CursorOptions(Supplier<LambdaQueryWrapper<T>> supplier, int batchSize,
                          String cursorColumn, Function<T, Long> idGetter) {
        this.supplier = supplier;
        this.batchSize = batchSize;
        this.cursorColumn = cursorColumn;
        this.idGetter = idGetter;
    }

    public static <T> CursorOptions<T> of(Supplier<LambdaQueryWrapper<T>> supplier, Function<T, Long> idGetter) {
        return new CursorOptions<>(supplier, 1000, "id", idGetter);
    }

    public CursorOptions<T> batchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public CursorOptions<T> cursorColumn(String cursorColumn) {
        this.cursorColumn = cursorColumn;
        return this;
    }

}
