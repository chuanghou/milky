package com.stellariver.milky.infrastructure.base.database;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stellariver.milky.common.base.ErrorEnumsBase;
import com.stellariver.milky.common.base.SysEx;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

/**
 * 基于主键游标的分批读取能力。
 *
 * <p>典型使用场景：</p>
 * <ul>
 *   <li>cursorConsumer: 仅在当前方法内逐条处理，最适合简单 ETL、逐条回调。</li>
 *   <li>cursorIterator: 需要把数据源交给其他组件时使用，例如 CSV/Excel 仓储接收 Iterator。</li>
 *   <li>cursorIterable: 目标 API 接收 Iterable 或 for-each 消费时使用，本质是 Iterator 的轻包装。</li>
 * </ul>
 */
public interface BaseMapperWithCursor<T> extends BaseMapper<T> {

    default void cursorConsumer(Consumer<T> consumer, CursorOptions<T> cursorOptions) {
        Iterator<T> iterator = cursorIterator(cursorOptions);
        while (iterator.hasNext()) {
            consumer.accept(iterator.next());
        }
    }

    default Iterator<T> cursorIterator(CursorOptions<T> cursorOptions) {
        SysEx.nullThrow(cursorOptions);
        SysEx.nullThrow(cursorOptions.getSupplier());
        SysEx.trueThrow(cursorOptions.getBatchSize() <= 0, ErrorEnumsBase.PARAM_FORMAT_WRONG.message("batchSize must be positive"));
        SysEx.nullThrow(cursorOptions.getIdGetter());
        String cursorColumn = cursorOptions.getCursorColumn();
        SysEx.trueThrow(cursorColumn == null || cursorColumn.trim().isEmpty(),
                ErrorEnumsBase.PARAM_FORMAT_WRONG.message("cursorColumn must not be blank"));
        return new Iterator<T>() {
            private long id = 0L;
            private List<T> currentBatch = null;
            private int index = 0;
            private boolean finished = false;

            @Override
            public boolean hasNext() {
                loadBatchIfNeeded();
                return !finished;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return currentBatch.get(index++);
            }

            private void loadBatchIfNeeded() {
                if (finished) {
                    return;
                }
                if (currentBatch != null && index < currentBatch.size()) {
                    return;
                }
                LambdaQueryWrapper<T> queryWrapper = cursorOptions.getSupplier().get()
                        .apply(cursorColumn + " > {0}", id)
                        .last("order by " + cursorColumn + " asc limit " + cursorOptions.getBatchSize());
                List<T> ts = selectList(queryWrapper);
                if (ts.isEmpty()) {
                    finished = true;
                    currentBatch = null;
                    return;
                }
                Long newId = ts.stream().map(cursorOptions.getIdGetter())
                        .max(Long::compareTo).orElseThrow(() -> new SysEx(ErrorEnumsBase.UNREACHABLE_CODE));
                SysEx.trueThrow(newId <= id, ErrorEnumsBase.UNREACHABLE_CODE.message("cursor does not advance, current id=" + id));
                id = newId;
                currentBatch = ts;
                index = 0;
            }
        };
    }

    default Iterable<T> cursorIterable(CursorOptions<T> cursorOptions) {
        return () -> cursorIterator(cursorOptions);
    }
}
