package com.stellariver.milky.common.tool.utils;

import com.google.common.collect.Sets;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Collect {

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
                .forEach(entry -> resultMap.computeIfAbsent(entry.getKey(), key -> new ArrayList<V>()).addAll(entry.getValue()));
        return resultMap;
    }

    public static <K, V> Map<K, List<V>> merge(Map<K, List<V>> map1, Map<K, V> map2) {
        map1 = Optional.ofNullable(map1).orElse(new HashMap<>());
        map2 = Optional.ofNullable(map2).orElse(new HashMap<>());
        HashMap<K, List<V>> map = new HashMap<>(map1);
        map2.forEach((key, value) -> map.computeIfAbsent(key, k -> new ArrayList<V>()).add(value));
        return map;
    }

    /**
     * 相比于map接口提供的那个merge函数，只能说有点意思吧，那么其实是利用旧值和和新值合并生成一个值，
     * 但是相比我这里的不是特别直观，用来统计数据，确实比较合适
     */
    public static <K, V> Map<K, V> subPriorMerge(Map<K, V> supMap, Map<K, V> subMap) {
        supMap = Optional.ofNullable(supMap).orElse(new HashMap<>());
        subMap = Optional.ofNullable(subMap).orElse(new HashMap<>());
        HashMap<K, V> map = new HashMap<>(supMap);
        subMap.forEach(map::put);
        return map;
    }

    public static <K, V> Map<K, V> supPriorMerge(Map<K, V> supMap, Map<K, V> subMap) {
        return subPriorMerge(subMap, supMap);
    }

    /**
     * 相同key保留第一个值
     */
    public static <K, V> Map<K, V> collectToMap(Collection<V> source, Function<V, K> mapper) {
        return stream(source).filter(Objects::nonNull).collect(Collectors.toMap(mapper, Function.identity(), (v1, v2) -> v1));
    }

    /**
     * 相同key保留第一个值
     */
    public static <T, K, V> Map<K, V> collectToMap(Collection<T> source, Function<T, K> keyMapper, Function<T, V> valueMapper) {
        return stream(source).filter(Objects::nonNull).collect(Collectors.toMap(keyMapper, valueMapper, (v1, v2) -> v1));
    }

    /**
     * 转为Map有可能抛出异常
     */
    public static <K, V> Map<K, V> collectToMapMightException(Collection<V> source, Function<V, K> mapper){
        return stream(source).filter(Objects::nonNull).collect(Collectors.toMap(mapper, Function.identity()));
    }

    /**
     * group
     */
    public static <K, V> Map<K, List<V>> group(Collection<V> source, Function<V, K> keyMapper){
        return stream(source).filter(Objects::nonNull).collect(Collectors.groupingBy(keyMapper));
    }

    public static <S, R, T extends Collection<R>> T convert(Collection<S> sourceCollection, Function<S, R> converter, Supplier<T> factory) {
        return stream(sourceCollection).filter(Objects::nonNull)
                .map(converter).filter(Objects::nonNull).collect(Collectors.toCollection(factory));
    }

    public static <S, R> List<R> convert(Collection<S> sourceCollection, Function<S, R> converter) {
        return convert(sourceCollection, converter, ArrayList::new);
    }

    public static <T extends Collection<V>, V> T delete(T source, Predicate<V> predicate, Supplier<T> factory) {
        return filter(source, predicate.negate(), factory);
    }

    public static <T extends Collection<V>, V> T filter(T source, Predicate<V> predicate, Supplier<T> factory) {
        return stream(source).filter(predicate).collect(Collectors.toCollection(factory));
    }

    public static <T> Set<T> difference(Set<T> set1, Set<T> set2) {
        return Sets.difference(set1, set2);
    }

    public static <T> Set<T> intersection(Set<T> set1, Set<T> set2) {
        return Sets.intersection(set1, set2);
    }

    public static <T> Set<T> symmetricDifference(Set<T> set1, Set<T> set2) {
        return Sets.symmetricDifference(set1, set2);
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return isEmpty(map);
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }
}
