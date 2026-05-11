package com.stellariver.milky.infrastructure.base.database;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.stellariver.milky.common.base.BizEx;
import com.stellariver.milky.common.base.ErrorEnumsBase;
import com.stellariver.milky.common.base.SysEx;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * {@link BaseMapper} 的增强：游标列分批遍历；乐观锁操作支持两种方式。
 *
 * <p>
 * 提供两种风格：
 * </p>
 * <ul>
 * <li>新增直接使用 {@link BaseMapper#insert(Object)}：不涉及乐观锁，要么插入成功（受影响行数大于 0），要么由数据库或框架抛异常</li>
 * <li>更新保留 {@code try*} 与 {@code *OrThrow} 两种风格（兼容乐观锁：受影响行数为 0 时不抛 SQL 异常）</li>
 * <li>删除统一为幂等逻辑删除：执行 {@code SET deleted=id}，不暴露成功/失败语义</li>
 * </ul>
 */
public interface MilkyMapper<T> extends BaseMapper<T> {

    String LOGIC_DELETE_COLUMN = "deleted";
    String KEY_COLUMN = "id";
    String LOGIC_DELETE_SQL = LOGIC_DELETE_COLUMN + " = " + KEY_COLUMN;

    /**
     * 尝试更新，返回是否成功。
     *
     * @param entity 实体对象
     * @return 成功返回 true，失败返回 false
     */
    default boolean tryUpdateById(T entity) {
        return updateById(entity) > 0;
    }

    /**
     * 更新，失败时抛出异常。
     *
     * @param entity 实体对象
     * @throws BizEx 受影响行数为 0 时抛出
     */
    default void updateByIdOrThrow(T entity) {
        if (!tryUpdateById(entity)) {
            throw new BizEx(ErrorEnumsBase.CONCURRENCY_VIOLATION);
        }
    }

    /**
     * 尝试更新，返回是否成功。
     *
     * @param entity        实体对象
     * @param updateWrapper 更新条件
     * @return 成功返回 true，失败返回 false
     */
    default boolean tryUpdate(T entity, Wrapper<T> updateWrapper) {
        return update(entity, updateWrapper) > 0;
    }

    /**
     * 更新，失败时抛出异常。
     *
     * @param entity        实体对象
     * @param updateWrapper 更新条件
     * @throws BizEx 受影响行数为 0 时抛出
     */
    default void updateOrThrow(T entity, Wrapper<T> updateWrapper) {
        if (!tryUpdate(entity, updateWrapper)) {
            throw new BizEx(ErrorEnumsBase.CONCURRENCY_VIOLATION);
        }
    }
    /**
     * 尝试删除，返回是否成功。
     *
     * @param id 主键 ID
     * @return 成功返回 true，失败返回 false
     */
    default boolean tryDeleteById(java.io.Serializable id) {
        return deleteById(id) > 0;
    }

    /**
     * 删除，失败时抛出异常。
     *
     * @param id 主键 ID
     * @throws BizEx 受影响行数为 0 时抛出
     */
    default void deleteByIdOrThrow(java.io.Serializable id) {
        if (!tryDeleteById(id)) {
            throw new BizEx(ErrorEnumsBase.CONCURRENCY_VIOLATION);
        }
    }

    /**
     * 尝试删除，返回是否成功。
     *
     * @param entity 实体对象
     * @return 成功返回 true，失败返回 false
     */
    default boolean tryDeleteById(T entity) {
        return deleteById(entity) > 0;
    }

    /**
     * 删除，失败时抛出异常。
     *
     * @param entity 实体对象
     * @throws BizEx 受影响行数为 0 时抛出
     */
    default void deleteByIdOrThrow(T entity) {
        if (!tryDeleteById(entity)) {
            throw new BizEx(ErrorEnumsBase.CONCURRENCY_VIOLATION);
        }
    }

    /**
     * 尝试删除，返回是否成功。
     *
     * @param columnMap 条件 Map
     * @return 成功返回 true，失败返回 false
     */
    default boolean tryDeleteByMap(Map<String, Object> columnMap) {
        return deleteByMap(columnMap) > 0;
    }

    /**
     * 删除，失败时抛出异常。
     *
     * @param columnMap 条件 Map
     * @throws BizEx 受影响行数为 0 时抛出
     */
    default void deleteByMapOrThrow(Map<String, Object> columnMap) {
        if (!tryDeleteByMap(columnMap)) {
            throw new BizEx(ErrorEnumsBase.CONCURRENCY_VIOLATION);
        }
    }

    /**
     * 尝试删除，返回是否成功。
     *
     * @param queryWrapper 删除条件
     * @return 成功返回 true，失败返回 false
     */
    default boolean tryDelete(Wrapper<T> queryWrapper) {
        return delete(queryWrapper) > 0;
    }

    /**
     * 删除，失败时抛出异常。
     *
     * @param queryWrapper 删除条件
     * @throws BizEx 受影响行数为 0 时抛出
     */
    default void deleteOrThrow(Wrapper<T> queryWrapper) {
        if (!tryDelete(queryWrapper)) {
            throw new BizEx(ErrorEnumsBase.CONCURRENCY_VIOLATION);
        }
    }

    /**
     * 尝试批量删除，返回是否成功。
     *
     * @param idList ID 列表
     * @return 成功返回 true，失败返回 false
     */
    default boolean tryDeleteBatchIds(Collection<?> idList) {
        return deleteBatchIds(idList) > 0;
    }

    /**
     * 批量删除，失败时抛出异常。
     *
     * @param idList ID 列表
     * @throws BizEx 受影响行数为 0 时抛出
     */
    default void deleteBatchIdsOrThrow(Collection<?> idList) {
        if (!tryDeleteBatchIds(idList)) {
            throw new BizEx(ErrorEnumsBase.CONCURRENCY_VIOLATION);
        }
    }
    /**
     * {@link #cursorIterator(CursorOptions)} 的增强：逐条交给 {@code consumer}。
     */
    default void cursorConsumer(Consumer<T> consumer, CursorOptions<T> cursorOptions) {
        Iterator<T> iterator = cursorIterator(cursorOptions);
        while (iterator.hasNext()) {
            consumer.accept(iterator.next());
        }
    }

    /**
     * 按 {@link CursorOptions} 游标列递增、分批 {@link #selectList}。
     */
    default Iterator<T> cursorIterator(CursorOptions<T> cursorOptions) {
        SysEx.nullThrow(cursorOptions);
        SysEx.nullThrow(cursorOptions.getSupplier());
        SysEx.trueThrow(cursorOptions.getBatchSize() <= 0,
                ErrorEnumsBase.PARAM_FORMAT_WRONG.message("batchSize must be positive"));
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
                SysEx.trueThrow(newId <= id,
                        ErrorEnumsBase.UNREACHABLE_CODE.message("cursor does not advance, current id=" + id));
                id = newId;
                currentBatch = ts;
                index = 0;
            }
        };
    }

    /**
     * {@link #cursorIterator(CursorOptions)} 的增强：{@link Iterable} 视图。
     */
    default Iterable<T> cursorIterable(CursorOptions<T> cursorOptions) {
        return () -> cursorIterator(cursorOptions);
    }

    /**
     * {@link #cursorIterator(CursorOptions)} 的增强：迭代元素经 {@code mapper} 映射为
     * {@code S}。
     */
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

    /**
     * {@link #cursorIterator(CursorOptions, Function)} 的增强：对映射结果调用
     * {@code consumer}。
     */
    default <S> void cursorConsumer(CursorOptions<T> cursorOptions,
                                    Function<? super T, ? extends S> mapper,
                                    Consumer<? super S> consumer) {
        SysEx.nullThrow(consumer);
        Iterator<S> iterator = cursorIterator(cursorOptions, mapper);
        while (iterator.hasNext()) {
            consumer.accept(iterator.next());
        }
    }

    /**
     * {@link #cursorIterator(CursorOptions, Function)} 的增强：{@link Iterable} 视图。
     */
    default <S> Iterable<S> cursorIterable(CursorOptions<T> cursorOptions, Function<? super T, ? extends S> mapper) {
        return () -> cursorIterator(cursorOptions, mapper);
    }

    /**
     * {@link #cursorIterator(CursorOptions)} 的增强：每条 {@code T} 经 {@code expander} 展开为 0..n 条
     * {@code S}，按序惰性串联为单个迭代器（仍为游标分批拉取，不会一次性加载全部 {@code T}）。
     */
    default <S> Iterator<S> cursorFlatIterator(CursorOptions<T> cursorOptions,
                                               Function<? super T, ? extends Iterable<? extends S>> expander) {
        SysEx.nullThrow(expander);
        Iterator<T> source = cursorIterator(cursorOptions);
        return new Iterator<S>() {
            private Iterator<? extends S> current = Collections.emptyIterator();

            private void advanceWhileNeeded() {
                while (!current.hasNext() && source.hasNext()) {
                    Iterable<? extends S> expanded = expander.apply(source.next());
                    current = expanded == null ? Collections.emptyIterator() : expanded.iterator();
                }
            }

            @Override
            public boolean hasNext() {
                advanceWhileNeeded();
                return current.hasNext();
            }

            @Override
            public S next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return current.next();
            }
        };
    }

    /**
     * {@link #cursorFlatIterator(CursorOptions, Function)} 的增强：{@link Iterable} 视图。
     */
    default <S> Iterable<S> cursorFlatIterable(CursorOptions<T> cursorOptions,
                                               Function<? super T, ? extends Iterable<? extends S>> expander) {
        return () -> cursorFlatIterator(cursorOptions, expander);
    }

    /**
     * {@link #cursorFlatIterator(CursorOptions, Function)} 的增强：逐条交给 {@code consumer}。
     */
    default <S> void cursorFlatConsumer(CursorOptions<T> cursorOptions,
                                        Function<? super T, ? extends Iterable<? extends S>> expander,
                                        Consumer<? super S> consumer) {
        SysEx.nullThrow(consumer);
        Iterator<S> iterator = cursorFlatIterator(cursorOptions, expander);
        while (iterator.hasNext()) {
            consumer.accept(iterator.next());
        }
    }
}
