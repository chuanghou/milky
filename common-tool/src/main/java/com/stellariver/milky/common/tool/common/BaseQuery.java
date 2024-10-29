package com.stellariver.milky.common.tool.common;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.stellariver.milky.common.base.ErrorEnumsBase;
import com.stellariver.milky.common.base.SysEx;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.common.tool.validate.ValidateUtil;
import lombok.*;
import lombok.experimental.FieldDefaults;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
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

    private Config config;

    private final ThreadLocal<Cache<ID, T>> threadLocal = new ThreadLocal<>();
    
    volatile private Cache<Set<ID>, Map<ID, T>> barrierCache;

    abstract public Map<ID, T> queryMapByIdsFilterEmptyIdsAfterCache(Set<ID> ids);

    public Map<ID, T> queryMapByIdsNotAllowLost(Set<ID> ids) {
        Map<ID, T> idtMap = queryMapByIds(ids);
        SysEx.trueThrowGet(!Kit.eq(idtMap.size(), ids.size()),
                () -> ErrorEnumsBase.NOT_ALLOW_LOST.message(Collect.subtract(ids, idtMap.keySet())));
        return idtMap;
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public Map<ID, T> queryMapByIds(Set<ID> ids) {

        if (Collect.isEmpty(ids)) {
            return Collections.EMPTY_MAP;
        }

        Map<ID, T> mapResult = new HashMap<>(16);
        Cache<ID, T> cache = threadLocal.get();
        Config config = getCacheConfig();
        if (config != null && cache == null) {
            Cache<ID, T> c = CacheBuilder.newBuilder().maximumSize(config.getTlcMaximumSize())
                    .expireAfterWrite(config.getTlcExpireAfterWrite(), config.getTimeUnit())
                    .build();
            threadLocal.set(c);
        }

        cache = threadLocal.get();
        if (cache != null && enable.get()) {
            Set<ID> cacheKeys = Collect.inter(ids, cache.asMap().keySet());
            for (ID cacheKey : cacheKeys) {
                T t = cache.getIfPresent(cacheKey);
                if (t != null) {
                    t = t == nullObject ? null : t;
                    mapResult.put(cacheKey, t);
                }
            }
            ids = Collect.subtract(ids, mapResult.keySet());
            if (Collect.isEmpty(ids)) {
                return mapResult;
            }
        }

        if (config != null && config.getBarrierCacheExpireAfterWrite() >= 0) {
            if (barrierCache == null) {
                synchronized (this) {
                    if (barrierCache == null) {
                        barrierCache = CacheBuilder.newBuilder()
                                .maximumSize(config.getBarrierCacheMaximumSize())
                                .expireAfterWrite(config.getBarrierCacheExpireAfterWrite(), config.getTimeUnit())
                                .build();
                    }
                }
            }
        }

        Map<ID, T> rpcResultMap;
        if (barrierCache != null) {
            Set<ID> finalIds = ids;
            rpcResultMap = barrierCache.get(ids, () -> queryMapByIdsFilterEmptyIdsAfterCache(finalIds));
        } else {
            rpcResultMap = queryMapByIdsFilterEmptyIdsAfterCache(ids);
        }

        if (enable.get() && cache != null) {
            for (Map.Entry<ID, T> entry : rpcResultMap.entrySet()) {
                T value = entry.getValue() == null ? nullObject : entry.getValue();
                cache.put(entry.getKey(), value);
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

    protected Config getCacheConfig() {
        if (config != null) {
            return config;
        }
        CacheConfig annotation0 = this.getClass().getSuperclass().getAnnotation(CacheConfig.class);
        CacheConfig annotation1 = this.getClass().getAnnotation(CacheConfig.class);
        boolean b = annotation0 != null && annotation1 != null;
        SysEx.trueThrow(b, ErrorEnumsBase.CONFIG_ERROR.message("exist two TLCConfigs"));
        CacheConfig annotation = annotation0 != null ? annotation0 : annotation1;
        if (annotation != null) {
            config = Config.builder().tlcMaximumSize(annotation.tlcMaximumSize())
                    .tlcExpireAfterWrite(annotation.tlcExpireAfterWrite())
                    .barrierCacheMaximumSize(annotation.barrierCacheMaximumSize())
                    .barrierCacheExpireAfterWrite(annotation.barrierCacheExpireAfterWrite())
                    .timeUnit(annotation.timeUnit())
                    .build();
            ValidateUtil.validate(config);
            return config;
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
    public static class Config {

        @NotNull
        @Positive
        Long tlcMaximumSize;

        @NotNull
        @Positive
        Long tlcExpireAfterWrite;

        @NotNull
        @PositiveOrZero
        long barrierCacheMaximumSize;

        @NotNull
        @PositiveOrZero
        long barrierCacheExpireAfterWrite;

        @NotNull
        TimeUnit timeUnit;

    }

}