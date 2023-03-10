package com.stellariver.milky.common.tool.common;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.stellariver.milky.common.tool.exception.ErrorEnumsBase;
import com.stellariver.milky.common.tool.exception.SysEx;
import com.stellariver.milky.common.tool.util.Collect;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author houchuang
 */
@SuppressWarnings("unused")
public abstract class BaseQuery<ID, T> {

    private final ThreadLocal<Boolean> enable = ThreadLocal.withInitial(() -> false);

    @SuppressWarnings("unchecked")
    private final T nullObject = (T) new Object();

    public T getT() {
        return nullObject;
    }

    private CacheConfig cacheConfig;

    private final ThreadLocal<Cache<ID, T>> threadLocal = new ThreadLocal<>();

    abstract public Map<ID, T> queryMapByIdsFilterEmptyIdsAfterCache(Set<ID> ids);

    public Map<ID, T> queryMapByIdsNotAllowLost(Set<ID> ids) {
        Map<ID, T> idtMap = queryMapByIds(ids);
        SysEx.trueThrowGet(!Kit.eq(idtMap.size(), ids.size()),
                () -> ErrorEnumsBase.NOT_ALLOW_LOST.message(Collect.diff(ids, idtMap.keySet())));
        return idtMap;
    }

    public Map<ID, T> queryMapByIds(Set<ID> ids) {
        Map<ID, T> mapResult = new HashMap<>(16);
        if (Collect.isEmpty(ids)) {
            return mapResult;
        }

        Cache<ID, T> cache = threadLocal.get();
        if (getCacheConfig() != null && cache == null) {
            Cache<ID, T> c = CacheBuilder.newBuilder().maximumSize(getCacheConfig().getMaximumSize())
                    .expireAfterWrite(getCacheConfig().getExpireAfterWrite(), getCacheConfig().getTimeUnit())
                    .build();
            threadLocal.set(c);
        }
        cache = threadLocal.get();
        if (enable.get() && threadLocal.get() != null) {
            Set<ID> cacheKeys = Collect.inter(ids, cache.asMap().keySet());
            for (ID cacheKey : cacheKeys) {
                T t = cache.getIfPresent(cacheKey);
                if (t != null) {
                    if (t == nullObject) {
                        mapResult.put(cacheKey, null);
                    } else {
                        mapResult.put(cacheKey, t);
                    }
                }
            }
            ids = Collect.diff(ids, mapResult.keySet());
            if (Collect.isEmpty(ids)) {
                return mapResult;
            }
        }

        Map<ID, T> rpcResultMap = queryMapByIdsFilterEmptyIdsAfterCache(ids);
        if (enable.get()) {
            for (Map.Entry<ID, T> entry : rpcResultMap.entrySet()) {
                if (entry.getValue() == null) {
                    cache.put(entry.getKey(), nullObject);
                } else {
                    cache.put(entry.getKey(), entry.getValue());
                }
            }
        }
        mapResult.putAll(rpcResultMap);
        return mapResult;
    }

    public void enableThreadLocal() {
        enable.set(true);
    }

    public void clearThreadLocal() {
        threadLocal.get().invalidateAll();
        enable.set(false);
    }

    protected CacheConfig getCacheConfig() {
        if (cacheConfig != null) {
            return cacheConfig;
        }
        TLCConfig annotation = this.getClass().getAnnotation(TLCConfig.class);
        if (annotation != null) {
            return CacheConfig.builder()
                    .maximumSize(annotation.maximumSize())
                    .expireAfterWrite(annotation.expireAfterWrite())
                    .timeUnit(annotation.timeUnit())
                    .build();
        }
        return null;
    }

    public Set<T> querySetByIdsNotAllowLost(Set<ID> ids) {
        return new HashSet<>(this.queryMapByIdsNotAllowLost(ids).values());
    }

    public Set<T> querySetByIds(Set<ID> ids) {
        return new HashSet<>(this.queryMapByIds(ids).values());
    }

    public List<T> queryListByIdsNotAllowLost(Set<ID> ids) {
        return new ArrayList<>(this.queryMapByIdsNotAllowLost(ids).values());
    }

    public List<T> queryListByIds(Set<ID> ids) {
        return new ArrayList<>(this.queryMapByIds(ids).values());
    }

    public Optional<T> queryByIdOptional(ID id) {
        Set<ID> ids = new HashSet<>(id == null ? Collections.emptyList() : Collections.singletonList(id));
        Map<ID, T> tMap = this.queryMapByIds(ids);
        return Optional.ofNullable(tMap).map(m -> m.get(id));
    }

    public T queryById(ID id) {
        Optional<T> optional = queryByIdOptional(id);
        return optional.orElseThrow(() -> new SysEx(ErrorEnumsBase.ENTITY_NOT_FOUND.message("id:" + id.toString())));
    }

    public Iterator<List<T>> buildIterator(Integer pageSize) {
        throw new SysEx(ErrorEnumsBase.CONFIG_ERROR.message("need to instantiate by sub class!"));
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CacheConfig {

        long maximumSize;

        long expireAfterWrite;

        TimeUnit timeUnit;

    }

}