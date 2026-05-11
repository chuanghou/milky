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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
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
 * <li>新增与更新保留 {@code try*} 与 {@code *OrThrow} 两种风格</li>
 * <li>删除统一为幂等逻辑删除：执行 {@code SET deleted=id}，不暴露成功/失败语义</li>
 * </ul>
 */
public interface MilkyMapper<T> extends BaseMapper<T> {

    String LOGIC_DELETE_COLUMN = "deleted";
    String KEY_COLUMN = "id";
    String LOGIC_DELETE_SQL = LOGIC_DELETE_COLUMN + " = " + KEY_COLUMN;

    /**
     * 尝试新增，返回是否成功。
     *
     * @param entity 实体对象
     * @return 成功返回 true，失败返回 false
     */
    default boolean tryInsert(T entity) {
        return insert(entity) > 0;
    }

    /**
     * 新增，失败时抛出异常。
     *
     * @param entity 实体对象
     * @throws BizEx 受影响行数为 0 时抛出
     */
    default void insertOrThrow(T entity) {
        if (!tryInsert(entity)) {
            throw new BizEx(ErrorEnumsBase.CONCURRENCY_VIOLATION);
        }
    }

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

    /** 幂等删除：将 {@code deleted} 字段更新为主键 {@code id}。 */
    default void markDeletedAsIdById(java.io.Serializable id) {
        UpdateWrapper<T> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql(LOGIC_DELETE_SQL).eq(KEY_COLUMN, id).eq(LOGIC_DELETE_COLUMN, 0);
        update(null, updateWrapper);
    }

    /** 幂等删除：将 {@code deleted} 字段更新为实体主键 {@code id}。 */
    default void markDeletedAsIdById(T entity) {
        Object id = extractIdValue(entity);
        if (id == null) {
            return;
        }
        markDeletedAsIdById((java.io.Serializable) id);
    }

    /** 幂等删除：按条件将 {@code deleted} 字段更新为主键列 {@code id}。 */
    default void markDeletedAsIdByMap(Map<String, Object> columnMap) {
        UpdateWrapper<T> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql(LOGIC_DELETE_SQL).allEq(columnMap, false).eq(LOGIC_DELETE_COLUMN, 0);
        update(null, updateWrapper);
    }

    /** 幂等删除：按查询条件将 {@code deleted} 字段更新为主键列 {@code id}。 */
    default void markDeletedAsId(Wrapper<T> queryWrapper) {
        List<java.io.Serializable> ids = selectList(queryWrapper).stream()
                .map(this::extractIdValue)
                .filter(Objects::nonNull)
                .map(id -> (java.io.Serializable) id)
                .collect(Collectors.toList());
        markDeletedAsIdByBatchIds(ids);
    }

    /** 幂等删除：按主键集合将 {@code deleted} 字段更新为主键列 {@code id}。 */
    default void markDeletedAsIdByBatchIds(Collection<?> idList) {
        if (idList == null || idList.isEmpty()) {
            return;
        }
        UpdateWrapper<T> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql(LOGIC_DELETE_SQL).in(KEY_COLUMN, idList).eq(LOGIC_DELETE_COLUMN, 0);
        update(null, updateWrapper);
    }

    default Object extractIdValue(T entity) {
        if (entity == null) {
            return null;
        }
        TableInfo tableInfo = TableInfoHelper.getTableInfo(entity.getClass());
        String keyProperty = tableInfo == null ? KEY_COLUMN : tableInfo.getKeyProperty();
        return ReflectionKit.getFieldValue(entity, keyProperty);
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

    /** {@link #cursorIterator(CursorOptions)} 的增强：{@link Iterable} 视图。 */
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

    /** {@link #cursorIterator(CursorOptions, Function)} 的增强：{@link Iterable} 视图。 */
    default <S> Iterable<S> cursorIterable(CursorOptions<T> cursorOptions, Function<? super T, ? extends S> mapper) {
        return () -> cursorIterator(cursorOptions, mapper);
    }
}
