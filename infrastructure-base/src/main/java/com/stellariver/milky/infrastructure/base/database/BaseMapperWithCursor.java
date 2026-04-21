package com.stellariver.milky.infrastructure.base.database;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stellariver.milky.common.base.ErrorEnumsBase;
import com.stellariver.milky.common.base.SysEx;

import java.util.List;
import java.util.function.Consumer;

public interface BaseMapperWithCursor<T> extends BaseMapper<T> {

    default void cursorConsumer(Consumer<T> consumer, CursorOptions<T> cursorOptions) {
        SysEx.nullThrow(cursorOptions);
        SysEx.nullThrow(cursorOptions.getSupplier());
        SysEx.trueThrow(cursorOptions.getBatchSize() <= 0, ErrorEnumsBase.PARAM_FORMAT_WRONG.message("batchSize must be positive"));
        SysEx.nullThrow(cursorOptions.getIdGetter());
        String cursorColumn = cursorOptions.getCursorColumn();
        SysEx.trueThrow(cursorColumn == null || cursorColumn.trim().isEmpty(),
                ErrorEnumsBase.PARAM_FORMAT_WRONG.message("cursorColumn must not be blank"));
        Long id = 0L;
        while (true) {
            LambdaQueryWrapper<T> queryWrapper = cursorOptions.getSupplier().get()
                    .apply(cursorColumn + " > {0}", id)
                    .last("order by " + cursorColumn + " asc limit " + cursorOptions.getBatchSize());
            List<T> ts = selectList(queryWrapper);
            if (ts.isEmpty()) {
                break;
            }
            ts.forEach(consumer);
            Long newId = ts.stream().map(cursorOptions.getIdGetter())
                    .max(Long::compareTo).orElseThrow(() -> new SysEx(ErrorEnumsBase.UNREACHABLE_CODE));
            SysEx.trueThrow(newId <= id, ErrorEnumsBase.UNREACHABLE_CODE.message("cursor does not advance, current id=" + id));
            id = newId;
        }
    }
}
