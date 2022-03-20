package com.stellariver.milky.common.tool.log;

import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Logger implements org.slf4j.Logger {

    private final org.slf4j.Logger log;

    private final ThreadLocal<MortalMap<String, String>> threadLocalContents = new ThreadLocal<>();

    private final ThreadLocal<MortalMap<String, String>> originalThreadLocalContents = new ThreadLocal<>();

    static public Logger getLogger(Class<?> clazz) {
        return new Logger(clazz);
    }

    private Logger(Class<?> clazz){
        this.log = LoggerFactory.getLogger(clazz);
    }

    public Logger withLogTag(Object value) {
        return with("logTag", value);
    }

    public Logger withLogTag(boolean test, Object value) {
        return with(test, "logTag", value);
    }

    public Logger with(Map<String, ?> infos) {
        infos.forEach(this::with);
        return this;
    }

    public Logger with(boolean test, String key, Object value) {
        if (test) {
            return with(key, value);
        }
        return this;
    }

    public Logger with(String key, Object value) {
        if (key == null) {
            log.error("log key shouldn't be null");
            return this;
        }
        MortalMap<String, String> contents = threadLocalContents.get();
        if (contents == null) {
            threadLocalContents.set(new MortalMap<>());
            contents = threadLocalContents.get();
        }
        if (contents.containsKey(key)) {
            log.error("log key:{} already exists, it may from duplicate keys in one log expression, or memory leak. " +
                    "It's very dangerous, there must have a log expression which did not end with info(), error() and so on", key);
        }
        contents.put(key, Objects.toString(value));
        return this;
    }

    private void beforeLog() {
        Map<String, String> logContents = threadLocalContents.get();
        if (logContents == null || logContents.isEmpty()) {
            // 如果没有没有存储任何信息，那就直接返回
            return;
        }
        if (originalThreadLocalContents.get() == null) {
            originalThreadLocalContents.set(new MortalMap<>());
        }
        logContents.forEach((k, v) -> {
            originalThreadLocalContents.get().put(k, MDC.get(k));
            MDC.put(k, v);
        });

        logContents.clear();
    }

    private void afterLog() {
        originalThreadLocalContents.get().forEach(MDC::put);
        originalThreadLocalContents.get().clear();
    }

    @Override
    public String getName() {
        return log.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        beforeLog();
        try {
            log.trace(msg);
        } finally {
            afterLog();
        }
    }

    @Override
    public void trace(String format, Object arg) {
        beforeLog();
        try {
            log.trace(format, arg);
        } finally {
            afterLog();
        }
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        beforeLog();
        try {
            log.trace(format, arg1, arg2);
        } finally {
            afterLog();
        }
    }

    @Override
    public void trace(String format, Object... arguments) {
        beforeLog();
        try {
            log.trace(format, arguments);
        } finally {
            afterLog();
        }
    }

    @Override
    public void trace(String msg, Throwable t) {
        beforeLog();
        try {
            log.trace(msg, t);
        } finally {
            afterLog();
        }
    }


    @Override
    public boolean isTraceEnabled(Marker marker) {
        return log.isTraceEnabled(marker);
    }


    @Override
    public void trace(Marker marker, String msg) {
        beforeLog();
        try {
            log.trace(marker, msg);
        } finally {
            afterLog();
        }
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        beforeLog();
        try {
            log.trace(marker, format, arg);
        } finally {
            afterLog();
        }
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        beforeLog();
        try {
            log.trace(marker, format, arg1, arg2);
        } finally {
            afterLog();
        }
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        beforeLog();
        try {
            log.trace(marker, format, argArray);
        } finally {
            afterLog();
        }
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        beforeLog();
        try {
            log.trace(marker, msg, t);
        } finally {
            afterLog();
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        beforeLog();
        try {
            log.debug(msg);
        } finally {
            afterLog();
        }
    }

    @Override
    public void debug(String format, Object arg) {
        beforeLog();
        try {
            log.debug(format, arg);
        } finally {
            afterLog();
        }
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        beforeLog();
        try {
            log.debug(format, arg1, arg2);
        } finally {
            afterLog();
        }
    }

    @Override
    public void debug(String format, Object... arguments) {
        beforeLog();
        try {
            log.debug(format, arguments);
        } finally {
            afterLog();
        }
    }

    @Override
    public void debug(String msg, Throwable t) {
        beforeLog();
        try {
            log.debug(msg, t);
        } finally {
            afterLog();
        }
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return log.isDebugEnabled(marker);
    }

    @Override
    public void debug(Marker marker, String msg) {
        beforeLog();
        try {
            log.debug(marker, msg);
        } finally {
            afterLog();
        }
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        beforeLog();
        try {
            log.debug(marker, format, arg);
        } finally {
            afterLog();
        }
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        beforeLog();
        try {
            log.debug(marker, format, arg1, arg2);
        } finally {
            afterLog();
        }
    }

    @Override
    public void debug(Marker marker, String format, Object... argArray) {
        beforeLog();
        try {
            log.debug(marker, format, argArray);
        } finally {
            afterLog();
        }
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        beforeLog();
        try {
            log.debug(marker, msg, t);
        } finally {
            afterLog();
        }
    }

    @Override
    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        beforeLog();
        try {
            log.info(msg);
        } finally {
            afterLog();
        }
    }

    @Override
    public void info(String format, Object arg) {
        beforeLog();
        try {
            log.info(format, arg);
        } finally {
            afterLog();
        }
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        beforeLog();
        try {
            log.info(format, arg1, arg2);
        } finally {
            afterLog();
        }
    }

    @Override
    public void info(String format, Object... arguments) {
        beforeLog();
        try {
            log.info(format, arguments);
        } finally {
            afterLog();
        }
    }

    @Override
    public void info(String msg, Throwable t) {
        beforeLog();
        try {
            log.info(msg, t);
        } finally {
            afterLog();
        }
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return log.isInfoEnabled(marker);
    }

    @Override
    public void info(Marker marker, String msg) {
        beforeLog();
        try {
            log.info(marker, msg);
        } finally {
            afterLog();
        }
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        beforeLog();
        try {
            log.info(marker, format, arg);
        } finally {
            afterLog();
        }
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        beforeLog();
        try {
            log.info(marker, format, arg1, arg2);
        } finally {
            afterLog();
        }
    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {
        beforeLog();
        try {
            log.info(marker, format, arguments);
        } finally {
            afterLog();
        }
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        beforeLog();
        try {
            log.info(marker, msg, t);
        } finally {
            afterLog();
        }
    }


    @Override
    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        beforeLog();
        try {
            log.warn(msg);
        } finally {
            afterLog();
        }
    }

    @Override
    public void warn(String format, Object arg) {
        beforeLog();
        try{
            log.warn(format, arg);
        } finally {
            afterLog();
        }
    }

    @Override
    public void warn(String format, Object... arguments) {
        beforeLog();
        try {
            log.warn(format, arguments);
        } finally {
            afterLog();
        }
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        beforeLog();
        try {
            log.warn(format, arg1, arg2);
        } finally {
            afterLog();
        }
    }

    @Override
    public void warn(String msg, Throwable t) {
        beforeLog();
        try {
            log.warn(msg, t);
        } finally {
            afterLog();
        }
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return log.isWarnEnabled(marker);
    }

    @Override
    public void warn(Marker marker, String msg) {
        beforeLog();
        try {
            log.warn(marker, msg);
        } finally {
            afterLog();
        }
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        beforeLog();
        try {
            log.warn(marker, format, arg);
        } finally {
            afterLog();
        }
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        beforeLog();
        try {
            log.warn(marker, format, arg1, arg2);
        } finally {
            afterLog();
        }
    }

    @Override
    public void warn(Marker marker, String format, Object... argArray) {
        beforeLog();
        try {
            log.warn(marker, format, argArray);
        } finally {
            afterLog();
        }
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        beforeLog();
        try {
            log.warn(marker, msg, t);
        } finally {
            afterLog();
        }
    }
    @Override
    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        beforeLog();
        try {
            log.error(msg);
        } finally {
            afterLog();
        }
    }

    @Override
    public void error(String format, Object arg) {
        beforeLog();
        try {
            log.error(format, arg);
        } finally {
            afterLog();
        }
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        beforeLog();
        try {
            log.error(format, arg1, arg2);
        } finally {
            afterLog();
        }
    }

    @Override
    public void error(String format, Object... arguments) {
        beforeLog();
        try {
            log.error(format, arguments);
        } finally {
            afterLog();
        }
    }

    @Override
    public void error(String msg, Throwable t) {
        beforeLog();
        try {
            log.error(msg, t);
        } finally {
            afterLog();
        }
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return log.isErrorEnabled(marker);
    }

    @Override
    public void error(Marker marker, String msg) {
        beforeLog();
        try {
            log.error(marker, msg);
        } finally {
            afterLog();
        }
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        beforeLog();
        try {
            log.error(marker, format, arg);
        } finally {
            afterLog();
        }
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        beforeLog();
        try {
            log.error(marker, format, arg1, arg2);
        } finally {
            afterLog();
        }
    }

    @Override
    public void error(Marker marker, String format, Object... argArray) {
        beforeLog();
        try {
            log.error(marker, format, argArray);
        } finally {
            afterLog();
        }
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        beforeLog();
        try {
            log.error(marker, msg, t);
        } finally {
            afterLog();
        }
    }

    public static class MortalMap<K, V> extends HashMap<K, V> {

        public static final long DEFAULT_LIVE = 100L;

        private final PriorityQueue<LiveKey<K>> queue = new PriorityQueue<>();

        @Override
        public V put(K key, V value) {
            return put(key, value, DEFAULT_LIVE);
        }

        public V put(K key, V value, long liveMillis) {
            if (key == null) {
                throw new NullPointerException("key is null");
            }
            if (liveMillis <= 10) {
                throw new IllegalArgumentException( "live time must be positive");
            }
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
