package com.stellariver.milky.common.tool.common;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.common.tool.util.Json;

import java.util.*;
import java.util.concurrent.TimeUnit;

public abstract class BaseQuery<ID, T> {

    /**
     * 全局开关，在每一个线程入口处，需要主动打开cache的开关，否则不会运行，当然如果主动打开了开关，那么自然也需要主动清理并关闭
     */
    static public ThreadLocal<Boolean> cacheSwitch = ThreadLocal.withInitial(() -> false);

    private final ThreadLocal<Cache<ID, T>> threadLocal = ThreadLocal.withInitial(
            () -> CacheBuilder.newBuilder()
                    .maximumSize(getCacheConfiguration().getMaximumSize())
                    .expireAfterWrite(getCacheConfiguration().getExpireAfterWrite(), getCacheConfiguration().getTimeUnit())
                    .build()
    );

    abstract public Map<ID, T> queryMapByIdsFilterEmptyIdsAfterCache(Set<ID> ids);

    public Map<ID, T> queryMapByIdsNotAllowLost(Set<ID> ids) {
        Map<ID, T> idtMap = queryMapByIds(ids);
        SysException.trueThrowGet(!Kit.eq(idtMap.size(), ids.size()),
                () -> ErrorEnumBase.NOT_ALLOW_LOST.message(Collect.diff(ids, idtMap.keySet())));
        return idtMap;
    }

    public Map<ID, T> queryMapByIds(Set<ID> ids) {
        Map<ID, T> mapResult = new HashMap<>();
        if (Collect.isEmpty(ids)) {
            return mapResult;
        }
        Cache<ID, T> cache = threadLocal.get();
        if (getCacheConfiguration().isEnable() && Boolean.TRUE.equals(cacheSwitch.get())) {
            Set<ID> cacheKeys = Collect.inter(ids, cache.asMap().keySet());
            for (ID cacheKey : cacheKeys) {
                mapResult.put(cacheKey, cache.getIfPresent(cacheKey));
            }
            ids = Collect.diff(ids, cacheKeys);
            if (Collect.isEmpty(ids)) {
                return mapResult;
            }
        }

        Map<ID, T> rpcResultMap = queryMapByIdsFilterEmptyIdsAfterCache(ids);
        if (getCacheConfiguration().isEnable() && Boolean.TRUE.equals(cacheSwitch.get())) {
            cache.putAll(rpcResultMap);
        }
        mapResult.putAll(rpcResultMap);
        return mapResult;
    }

    public void clearThreadLocal() {
        threadLocal.get().invalidateAll();
    }

    public CacheConfig getCacheConfiguration() {
        return CacheConfig.builder().enable(false).maximumSize(1000L).expireAfterWrite(3000L).timeUnit(TimeUnit.MILLISECONDS).build();
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
        return optional.orElseThrow(() -> new SysException(ErrorEnumBase.ENTITY_NOT_FOUND.message("id:" + Json.toJson(id))));
    }

    public Iterator<List<T>> buildIterator(Integer pageSize) {
        throw new SysException(ErrorEnumBase.CONFIG_ERROR.message("need to instantiate by sub class!"));
    }

}