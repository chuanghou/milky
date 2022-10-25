package com.stellariver.milky.common.tool.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.stellariver.milky.common.tool.exception.ErrorEnumsBase;
import com.stellariver.milky.common.tool.exception.SysException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Collect {

    @SafeVarargs
    public static <T> List<T> asList(T... t) {
        return Arrays.stream(t).collect(Collectors.toList());
    }

    @SafeVarargs
    public static <T> Set<T> asSet(T... t) {
        return Arrays.stream(t).collect(Collectors.toSet());
    }

    public static <K, V> Map<K, V> asMap(K k, V v) {
        return StreamMap.init(k, v).getMap();
    }

    public static <K, V> Map<K, V> asMap(K k0, V v0, K k1, V v1) {
        return StreamMap.<K, V>init().put(k0, v0).put(k1, v1).getMap();
    }

    public static <K, V> Map<K, V> asMap(K k0, V v0, K k1, V v1, K k2, V v2) {
        return StreamMap.<K, V>init().put(k0, v0).put(k1, v1).put(k2, v2).getMap();
    }

    public static <K, V> Map<K, V> asMap(K k0, V v0, K k1, V v1, K k2, V v2, K k3, V v3) {
        return StreamMap.<K, V>init().put(k0, v0).put(k1, v1).put(k2, v2).put(k3, v3).getMap();
    }

    public static <T> Stream<T> stream(Collection<T> source) {
        return source != null ? source.stream() : Stream.empty();
    }

    @SafeVarargs
    public static <K, V> Map<K, List<V>> merge(Map<K, V>... maps) {
        HashMap<K, List<V>> resultMap = new HashMap<>();
        Arrays.stream(maps).flatMap(map -> map == null ? Stream.empty() : map.entrySet().stream())
                .forEach(entry -> resultMap.computeIfAbsent(entry.getKey(), key -> new ArrayList<>()).add(entry.getValue()));
        return resultMap;
    }

    @SafeVarargs
    public static <K, V> Map<K, List<V>> reGroup(Map<K, List<V>>... maps) {
        HashMap<K, List<V>> resultMap = new HashMap<>();
        Arrays.stream(maps).flatMap(map -> map == null ? Stream.empty() : map.entrySet().stream())
                .forEach(entry -> resultMap.computeIfAbsent(entry.getKey(), key -> new ArrayList<>()).addAll(entry.getValue()));
        return resultMap;
    }

    public static <K, V> Map<K, List<V>> merge(Map<K, List<V>> map1, Map<K, V> map2) {
        map1 = Kit.op(map1).orElse(new HashMap<>());
        map2 = Kit.op(map2).orElse(new HashMap<>());
        HashMap<K, List<V>> map = new HashMap<>(map1);
        map2.forEach((key, value) -> map.computeIfAbsent(key, k -> new ArrayList<>()).add(value));
        return map;
    }
    public static <K, V> Map<K, V> mergeMightException(Map<K, V> map0, Map<K, V> map1) {
        map0 = Kit.op(map0).orElseGet(HashMap::new);
        map1 = Kit.op(map1).orElseGet(HashMap::new);
        Set<K> inter = Collect.inter(map0.keySet(), map1.keySet());
        SysException.trueThrowGet(Collect.isNotEmpty(inter), () -> ErrorEnumsBase.MERGE_EXCEPTION);
        HashMap<K, V> resultMap = new HashMap<>(map0);
        resultMap.putAll(map1);
        return resultMap;
    }

    public static <K, V> Map<K, V> subPriorMerge(Map<K, V> supMap, Map<K, V> subMap) {
        supMap = Kit.op(supMap).orElseGet(HashMap::new);
        subMap = Kit.op(subMap).orElseGet(HashMap::new);
        HashMap<K, V> map = new HashMap<>(supMap);
        map.putAll(subMap);
        return map;
    }

    public static <K, V> Map<K, V> supPriorMerge(Map<K, V> supMap, Map<K, V> subMap) {
        return subPriorMerge(subMap, supMap);
    }

    /**
     * 相同key保留第一个值
     */
    public static <K, V> Map<K, V> toMap(Collection<V> source, Function<V, K> mapper) {
        return stream(source).filter(Objects::nonNull).collect(Collectors.toMap(mapper, Function.identity(), (v1, v2) -> v1));
    }

    /**
     * 相同key保留第一个值
     */
    public static <T, K, V> Map<K, V> toMap(Collection<T> source, Function<T, K> keyMapper, Function<T, V> valueMapper) {
        return stream(source).filter(Objects::nonNull).collect(Collectors.toMap(keyMapper, valueMapper, (v1, v2) -> v1));
    }

    /**
     * 转为Map有可能抛出异常
     */
    public static <K, V> Map<K, V> toMapMightException(Collection<V> source, Function<V, K> mapper){
        return stream(source).filter(Objects::nonNull).collect(Collectors.toMap(mapper, Function.identity()));
    }

    /**
     * group
     */
    public static <K, V> Map<K, List<V>> group(Collection<V> source, Function<V, K> keyMapper){
        return stream(source).filter(Objects::nonNull).collect(Collectors.groupingBy(keyMapper));
    }

    /**
     * group
     */
    public static <T, K, V> Map<K, List<V>> groupList(Collection<T> source, Function<T, K> keyMapper, Function<T, V> valueMapper){
        HashMap<K, List<V>> resultMap = new HashMap<>();
        stream(source).filter(Objects::nonNull)
                .forEach(t -> resultMap.computeIfAbsent(keyMapper.apply(t), i -> new ArrayList<>()).add(valueMapper.apply(t)));
        return resultMap;
    }

    /**
     * group
     */
    public static <T, K, V> Map<K, Set<V>> groupSet(Collection<T> source, Function<T, K> keyMapper, Function<T, V> valueMapper){
        HashMap<K, Set<V>> resultMap = new HashMap<>();
        stream(source).filter(Objects::nonNull)
                .forEach(t -> resultMap.computeIfAbsent(keyMapper.apply(t), i -> new HashSet<>()).add(valueMapper.apply(t)));
        return resultMap;
    }

    public static <S, R, T extends Collection<R>> T transfer(Collection<S> sourceCollection, Function<S, R> converter, Supplier<T> factory) {
        return stream(sourceCollection).filter(Objects::nonNull)
                .map(converter).filter(Objects::nonNull).collect(Collectors.toCollection(factory));
    }

    public static <S, R> List<R> transfer(Collection<S> sourceCollection, Function<S, R> converter) {
        return transfer(sourceCollection, converter, ArrayList::new);
    }

    public static <T extends Collection<V>, V> T delete(T source, Predicate<V> predicate, Supplier<T> factory) {
        return filter(source, predicate.negate(), factory);
    }

    public static <T extends Collection<V>, V> T filter(T source, Predicate<V> predicate, Supplier<T> factory) {
        return stream(source).filter(predicate).collect(Collectors.toCollection(factory));
    }

    public static <T> Set<T> diff(Collection<T> collection1, Collection<T> collection2) {
        Set<T> set1 = Kit.op(collection1).map(HashSet::new).orElseGet(HashSet::new);
        Set<T> set2 = Kit.op(collection2).map(HashSet::new).orElseGet(HashSet::new);
        return Sets.difference(set1, set2);
    }

    public static <T> Set<T> inter(Collection<T> collection1, Collection<T> collection2) {
        Set<T> set1 = Kit.op(collection1).map(HashSet::new).orElseGet(HashSet::new);
        Set<T> set2 =Kit.op(collection2).map(HashSet::new).orElseGet(HashSet::new);
        return Sets.intersection(set1, set2);
    }

    public static <T> Set<T> symmetric(Collection<T> collection1, Collection<T> collection2) {
        Set<T> set1 = Kit.op(collection1).map(HashSet::new).orElseGet(HashSet::new);
        Set<T> set2 = Kit.op(collection2).map(HashSet::new).orElseGet(HashSet::new);
        return Sets.symmetricDifference(set1, set2);
    }

    public static boolean isEmpty(Object object) {
        return CollectionUtils.size(object) == 0;
    }

    public static boolean isNotEmpty(Object object) {
        return !isEmpty(object);
    }

    public static int size(final Object object) {
        return CollectionUtils.size(object);
    }

    public <T> List<List<T>> partition(List<T> ts, int size) {
        return Lists.partition(ts, size);
    }

    public <K, V> V getV(Map<? super K, V> map, K key) {
        return MapUtils.getObject(map, key);
    }

}
