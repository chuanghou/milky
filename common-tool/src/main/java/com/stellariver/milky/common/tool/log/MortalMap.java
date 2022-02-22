package com.stellariver.milky.common.tool.log;

import com.sun.tools.javac.util.Assert;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class MortalMap<K, V> extends HashMap<K, V> {

    public static final long DEFAULT_LIVE = 100L;

    private final PriorityQueue<LiveKey<K>> queue = new PriorityQueue<>();

    @Override
    public V put(K key, V value) {
        return put(key, value, DEFAULT_LIVE);
    }

    public V put(K key, V value, long liveMillis) {
        Assert.checkNonNull(key);
        Assert.check(liveMillis > 0, "live time must be positive");
        long currentTimeMillis = System.currentTimeMillis();
        V result = super.put(key, value);
        queue.add(new LiveKey<>(key, currentTimeMillis + liveMillis));
        removeDeath(currentTimeMillis);
        return result;
    }

    @Override
    public V get(Object key) {
        removeDeath(System.currentTimeMillis());
        return super.get(key);
    }

    @Override
    public boolean containsKey(Object key) {
        removeDeath(System.currentTimeMillis());
        return super.containsKey(key);
    }

    @Override
    public boolean isEmpty() {
        removeDeath(System.currentTimeMillis());
        return super.isEmpty();
    }

    @Override
    public boolean containsValue(Object value) {
        removeDeath(System.currentTimeMillis());
        return super.containsValue(value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        removeDeath(System.currentTimeMillis());
        return super.replace(key, oldValue, newValue);
    }

    @Override
    public Set<K> keySet() {
        removeDeath(System.currentTimeMillis());
        return super.keySet();
    }

    @Override
    public int size() {
        removeDeath(System.currentTimeMillis());
        return super.size();
    }

    @Override
    public Collection<V> values() {
        removeDeath(System.currentTimeMillis());
        return super.values();
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        removeDeath(System.currentTimeMillis());
        super.replaceAll(function);
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        removeDeath(System.currentTimeMillis());
        return super.entrySet();
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        removeDeath(System.currentTimeMillis());
        return super.getOrDefault(key, defaultValue);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        removeDeath(System.currentTimeMillis());
        return super.putIfAbsent(key, value);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        removeDeath(System.currentTimeMillis());
        super.forEach(action);
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        removeDeath(System.currentTimeMillis());
        return super.merge(key, value, remappingFunction);
    }

    @Override
    public V replace(K key, V value) {
        removeDeath(System.currentTimeMillis());
        return super.replace(key, value);
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        removeDeath(System.currentTimeMillis());
        return super.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        removeDeath(System.currentTimeMillis());
        return super.computeIfPresent(key, remappingFunction);
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        removeDeath(System.currentTimeMillis());
        return super.compute(key, remappingFunction);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
       super.putAll(m);
       long currentTimeMillis = System.currentTimeMillis();
       m.forEach((k, v) -> queue.add(new LiveKey<>(k, currentTimeMillis + DEFAULT_LIVE)));
       removeDeath(System.currentTimeMillis());
    }

    private void removeDeath(long currentTimeMillis) {
        LiveKey<K> oldestLiveKey;
        while (true) {
            oldestLiveKey  = queue.peek();
            if (oldestLiveKey == null || oldestLiveKey.deathMillis > currentTimeMillis) {
                break;
            }
            LiveKey<K> removeLiveKey = queue.remove();
            super.remove(removeLiveKey.key);
        }
    }

    static private class LiveKey<K> implements Comparable<LiveKey<K>>{

        private final K key;

        private final long deathMillis;

        public LiveKey(K key, long deathMillis) {
            this.key = key;
            this.deathMillis = deathMillis;
        }

        @Override
        public int compareTo(LiveKey o) {
            return Long.compare(this.deathMillis, o.deathMillis);
        }

    }
}
