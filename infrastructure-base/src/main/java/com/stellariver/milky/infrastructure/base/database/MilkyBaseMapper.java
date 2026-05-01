package com.stellariver.milky.infrastructure.base.database;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stellariver.milky.common.base.ErrorEnumsBase;
import com.stellariver.milky.common.base.SysEx;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * {@link BaseMapper} 的增强：游标列分批遍历；{@code *WithOptimisticLock} 要求委托执行后必有受影响行。
 */
public interface MilkyBaseMapper<T> extends BaseMapper<T> {

    /**
     * {@link BaseMapper#updateById(Object)} 的增强。
     *
     * @throws LockConflictException 受影响行数为 0
     */
    default int updateByIdWithOptimisticLock(T entity) throws LockConflictException {
        int rows = updateById(entity);
        LockConflictExpect.assertAffected("updateById", rows);
        return rows;
    }

    /**
     * {@link BaseMapper#update(Object, Wrapper)} 的增强。
     *
     * @throws LockConflictException 受影响行数为 0
     */
    default int updateWithOptimisticLock(T entity, Wrapper<T> updateWrapper) throws LockConflictException {
        int rows = update(entity, updateWrapper);
        LockConflictExpect.assertAffected("update", rows);
        return rows;
    }

    /**
     * {@link BaseMapper#deleteById(java.io.Serializable)} 的增强。
     *
     * @throws LockConflictException 受影响行数为 0
     */
    default int deleteByIdWithOptimisticLock(java.io.Serializable id) throws LockConflictException {
        int rows = deleteById(id);
        LockConflictExpect.assertAffected("deleteById", rows);
        return rows;
    }

    /**
     * {@link BaseMapper#deleteById(Object)} 的增强。
     *
     * @throws LockConflictException 受影响行数为 0
     */
    default int deleteByIdWithOptimisticLock(T entity) throws LockConflictException {
        int rows = deleteById(entity);
        LockConflictExpect.assertAffected("deleteById(entity)", rows);
        return rows;
    }

    /**
     * {@link BaseMapper#deleteByMap(Map)} 的增强。
     *
     * @throws LockConflictException 受影响行数为 0
     */
    default int deleteByMapWithOptimisticLock(Map<String, Object> columnMap) throws LockConflictException {
        int rows = deleteByMap(columnMap);
        LockConflictExpect.assertAffected("deleteByMap", rows);
        return rows;
    }

    /**
     * {@link BaseMapper#delete(Wrapper)} 的增强。
     *
     * @throws LockConflictException 受影响行数为 0
     */
    default int deleteWithOptimisticLock(Wrapper<T> queryWrapper) throws LockConflictException {
        int rows = delete(queryWrapper);
        LockConflictExpect.assertAffected("delete", rows);
        return rows;
    }

    /**
     * {@link BaseMapper#deleteBatchIds(Collection)} 的增强。
     *
     * @throws LockConflictException 受影响行数为 0
     */
    default int deleteBatchIdsWithOptimisticLock(Collection<?> idList) throws LockConflictException {
        int rows = deleteBatchIds(idList);
        LockConflictExpect.assertAffected("deleteBatchIds", rows);
        return rows;
    }

    /** {@link #cursorIterator(CursorOptions)} 的增强：逐条交给 {@code consumer}。 */
    default void cursorConsumer(Consumer<T> consumer, CursorOptions<T> cursorOptions) {
        Iterator<T> iterator = cursorIterator(cursorOptions);
        while (iterator.hasNext()) {
            consumer.accept(iterator.next());
        }
    }

    /** 按 {@link CursorOptions} 游标列递增、分批 {@link #selectList}。 */
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

    /** {@link #cursorIterator(CursorOptions)} 的增强：{@link Iterable} 视图。 */
    default Iterable<T> cursorIterable(CursorOptions<T> cursorOptions) {
        return () -> cursorIterator(cursorOptions);
    }

    /** {@link #cursorIterator(CursorOptions)} 的增强：迭代元素经 {@code mapper} 映射为 {@code S}。 */
    default <S> Iterator<S> cursorIterator(CursorOptions<T> cursorOptions, Function<? super T, ? extends S> mapper) {
        SysEx.nullThrow(mapper);
        Iterator<T> source = cursorIterator(cursorOptions);
        return new Iterator<S>() {
            @Override
            public boolean hasNext() {
                return source.hasNext();
            }

            @Override
            public S next() {
                return mapper.apply(source.next());
            }
        };
    }

    /** {@link #cursorIterator(CursorOptions, Function)} 的增强：对映射结果调用 {@code consumer}。 */
    default <S> void cursorConsumer(CursorOptions<T> cursorOptions,
                                   Function<? super T, ? extends S> mapper,
                                   Consumer<? super S> consumer) {
        SysEx.nullThrow(consumer);
        Iterator<S> iterator = cursorIterator(cursorOptions, mapper);
        while (iterator.hasNext()) {
            consumer.accept(iterator.next());
        }
    }

    /** {@link #cursorIterator(CursorOptions, Function)} 的增强：{@link Iterable} 视图。 */
    default <S> Iterable<S> cursorIterable(CursorOptions<T> cursorOptions, Function<? super T, ? extends S> mapper) {
        return () -> cursorIterator(cursorOptions, mapper);
    }
}
