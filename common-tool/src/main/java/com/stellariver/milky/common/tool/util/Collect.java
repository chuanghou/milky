package com.stellariver.milky.common.tool.util;

import com.google.common.collect.*;
import com.google.common.collect.Lists;
import com.stellariver.milky.common.tool.common.Kit;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author houchuang
 */
@SuppressWarnings("unused")
public class Collect {

    public static <T, A, R> R collect(Collection<T> collection, Collector<T, A, R> collector) {
        return stream(collection).collect(collector);
    }
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
    public static <K, V> Multimap<K, V> merge(Map<K, V>... maps) {
        Multimap<K, V> resultMap = ArrayListMultimap.create();
        Arrays.stream(maps).filter(Objects::nonNull).flatMap(m -> m.entrySet().stream())
                .forEach(entry -> resultMap.put(entry.getKey(), entry.getValue()));
        return resultMap;
    }

    @SafeVarargs
    public static <K, V> Map<K, V> mergeMightEx(Map<K, V>... maps) {
        Map<K, V> result = new HashMap<>();
        Arrays.stream(maps).flatMap(map -> map.entrySet().stream()).forEach(e -> {
            V put = result.put(e.getKey(), e.getValue());
            if (put != null) {
                throw new IllegalStateException(String.format("Duplicate key %s", e.getKey()));
            }
        });
        return result;
    }


    @SafeVarargs
    public static <K, V> Multimap<K, V> reGroup(Map<K, List<V>>... maps) {
        Multimap<K, V> resultMap = ArrayListMultimap.create();
        Arrays.stream(maps).filter(Objects::nonNull).flatMap(map -> map.entrySet().stream())
                .forEach(entry -> resultMap.putAll(entry.getKey(), entry.getValue()));
        return resultMap;
    }

    public static <K, V> Multimap<K, V> merge(Map<K, List<V>> map1, Map<K, V> map2) {
        Multimap<K, V> resultMap = ArrayListMultimap.create();
        if (map1 != null) {
            map1.forEach(resultMap::putAll);
        }
        if (map2 != null) {
            map2.forEach(resultMap::put);
        }
        return resultMap;
    }

    public static <K, V> Map<K, V> priorMerge(Map<K, V> supMap, Map<K, V> subMap) {
        supMap = Kit.op(supMap).orElseGet(HashMap::new);
        subMap = Kit.op(subMap).orElseGet(HashMap::new);
        HashMap<K, V> map = new HashMap<>(supMap);
        map.putAll(subMap);
        return map;
    }

    public static <K, V> Map<K, V> toMap(Collection<V> source, Function<V, K> mapper) {
        return stream(source).filter(Objects::nonNull).collect(Collectors.toMap(mapper, Function.identity(), (v1, v2) -> v1));
    }

    public static <T, K, V> Map<K, V> toMap(Collection<T> source, Function<T, K> keyMapper, Function<T, V> valueMapper) {
        return stream(source).filter(Objects::nonNull).collect(Collectors.toMap(keyMapper, valueMapper, (v1, v2) -> v1));
    }

    @SuppressWarnings("unchecked")
    public static <T, K, V> Map<K, V> toMap(T[] source, Function<T, K> keyMapper, Function<T, V> valueMapper) {
        if (source == null) {
            return Collections.EMPTY_MAP;
        }
        return Arrays.stream(source).filter(Objects::nonNull).collect(Collectors.toMap(keyMapper, valueMapper, (v1, v2) -> v1));
    }

    public static <K, V> Map<K, V> toMapMightEx(Collection<V> source, Function<V, K> mapper){
        return stream(source).filter(Objects::nonNull).collect(Collectors.toMap(mapper, Function.identity()));
    }


    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> toMapMightEx(V[] source, Function<V, K> mapper){
        if (source == null) {
            return Collections.EMPTY_MAP;
        }
        return Arrays.stream(source).filter(Objects::nonNull).collect(Collectors.toMap(mapper, Function.identity()));
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

    public static <T> List<T> filter(T[] source, Predicate<T> predicate) {
        return Arrays.stream(source).filter(predicate).collect(Collectors.toList());
    }

    public static <T> Set<T> subtract(Collection<T> collection1, Collection<T> collection2) {
        Set<T> set1 = new HashSet<>();
        Set<T> set2 = new HashSet<>();
        collection2.forEach(c -> {
            if (c != null) {
                set2.add(c);
            }
        });
        collection1.forEach(c -> {
            if (c != null && !set2.contains(c)) {
                set1.add(c);
            }
        });
        return set1;
    }

    public static <T> Set<T> inter(Collection<T> collection1, Collection<T> collection2) {
        Set<T> set1 = new HashSet<>();
        Set<T> set2 = new HashSet<>();
        collection1.forEach(c -> {
            if (c != null) {
                set1.add(c);
            }
        });
        collection2.forEach(c -> {
            if (c != null && set1.contains(c)) {
                set2.add(c);
            }
        });
        return set2;
    }

    public static <T> Set<T> union(Collection<T> collection1, Collection<T> collection2) {
        Set<T> set = new HashSet<>();
        collection1.forEach(c -> {
            if (c != null) {
                set.add(c);
            }
        });
        collection2.forEach(c -> {
            if (c != null) {
                set.add(c);
            }
        });
        return set;
    }

    public static <T, K>  Collector<T, ?, Map<K, T>> toMapMightEx(Function<? super T, ? extends K> keyMapper) {
        return Collectors.toMap(keyMapper, v -> v);
    }

    @SafeVarargs
    public static <T> Collector<T, List<List<T>>, List<List<T>>> select(Predicate<T>... predicates) {
        return new Collector<T, List<List<T>>, List<List<T>>>() {
            @Override
            public Supplier<List<List<T>>> supplier() {
                return () -> {
                    List<List<T>> container = new ArrayList<>();
                    for (int i = 0; i < predicates.length; i++) {
                        container.add(new ArrayList<>());
                    }
                    return container;
                };
            }

            @Override
            public BiConsumer<List<List<T>>, T> accumulator() {
                return (container, t) -> {
                    for (int i = 0; i < predicates.length; i++) {
                        boolean test = predicates[i].test(t);
                        if (test) {
                            container.get(i).add(t);
                        }
                    }
                };
            }

            @Override
            public BinaryOperator<List<List<T>>> combiner() {
                return (container0, container1) -> {
                    for (int i = 0; i < predicates.length; i++) {
                        container0.get(i).addAll(container1.get(i));
                    }
                    return container0;
                };
            }

            @Override
            public Function<List<List<T>>, List<List<T>>> finisher() {
                return Function.identity();
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.singleton(Characteristics.IDENTITY_FINISH);
            }

        };
    }

    public static <T, K> Collector<T, Multimap<K, T>, Multimap<K, T>> multiMap(
            Function<? super T, ? extends K> keyMapper,
            boolean distinct) {
        return multiMap(keyMapper, Function.identity(), distinct);
    }

    public static <T, K, V> Collector<T, Multimap<K, V>, Multimap<K, V>> multiMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper,
            boolean distinct) {
        return new Collector<T, Multimap<K, V>, Multimap<K, V>>() {
            @Override
            public Supplier<Multimap<K, V>> supplier() {
                return distinct ? () -> MultimapBuilder.hashKeys().arrayListValues().build()
                        : () -> MultimapBuilder.hashKeys().hashSetValues().build();
            }

            @Override
            public BiConsumer<Multimap<K, V>, T> accumulator() {
                return (container, t) -> container.put(keyMapper.apply(t), valueMapper.apply(t));
            }

            @Override
            public BinaryOperator<Multimap<K, V>> combiner() {
                return (container0, container1) -> {
                     container0.putAll(container1);
                     return container0;
                };
            }

            @Override
            public Function<Multimap<K, V>,Multimap<K, V>> finisher() {
                return Function.identity();
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.singleton(Characteristics.IDENTITY_FINISH);
            }

        };
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

    public static <T> List<List<T>> partition(List<T> ts, int size) {
        return Lists.partition(ts, size);
    }

    public static <K, V> V getV(Map<? super K, V> map, K key) {
        return MapUtils.getObject(map, key);
    }

}
