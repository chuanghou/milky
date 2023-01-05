package com.stellariver.milky.common.tool.log;

import com.stellariver.milky.common.tool.exception.BizException;
import com.stellariver.milky.common.tool.common.Clock;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.common.tool.util.Json;
import lombok.EqualsAndHashCode;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Logger implements org.slf4j.Logger {

    private final org.slf4j.Logger log;

    private final ThreadLocal<MortalMap<String, String>> threadLocalContents = ThreadLocal.withInitial(MortalMap::new);

    private final ThreadLocal<MortalMap<String, String>> originalThreadLocalContents = ThreadLocal.withInitial(MortalMap::new);

    static public Logger getLogger(Class<?> clazz) {
        return new Logger(clazz);
    }

    public void clear() {
        threadLocalContents.get().clear();
    }

    public void log(String logTag, Throwable throwable) {
        if (throwable == null) {
            this.success(true).info(logTag);
        } else if (throwable instanceof BizException) {
            this.success(false).warn(logTag, throwable);
        } else {
            this.success(false).error(logTag, throwable);
        }
    }

    public void withSwitch(boolean sw, String logTag) {
        withSwitch(sw, logTag, null);
    }

    public void withSwitch(boolean sw, String logTag, Throwable throwable) {
        if (sw || throwable != null) {
            if (throwable == null) {
                info(logTag);
            } else {
                error(logTag, throwable);
            }
        } else {
            clear();
        }
    }

    public void logWhenException(String logTag, Throwable throwable) {
        if (throwable == null) {
            threadLocalContents.get().clear();
            return;
        }
        if (throwable instanceof BizException) {
            this.success(false).warn(logTag, throwable);
        } else {
            this.success(false).error(logTag, throwable);
        }
    }

    private Logger(Class<?> clazz){
        this.log = LoggerFactory.getLogger(clazz);
    }

    public Logger with(Map<String, Object> infos) {
        infos.forEach(this::with);
        return this;
    }

    public Logger arg0(Object value) {
        with("arg0", value);
        return this;
    }

    public Logger arg1(Object value) {
        with("arg1", value);
        return this;
    }

    public Logger arg2(Object value) {
        with("arg2", value);
        return this;
    }

    public Logger arg3(Object value) {
        with("arg3", value);
        return this;
    }

    public Logger arg4(Object value) {
        with("arg4", value);
        return this;
    }

    public Logger source(Object value) {
        with("source", value);
        return this;
    }

    public Logger result(Object value) {
        with("result", value);
        return this;
    }

    public Logger cost(Object value) {
        with("cost", value);
        return this;
    }

    public Logger success(Object value) {
        with("success", value);
        return this;
    }

    public Logger with(String key, Object value) {
        if (key == null) {
            log.error("log key shouldn't be null");
            return this;
        }
        MortalMap<String, String> contents = threadLocalContents.get();
        if (contents.containsKey(key)) {
            log.error("log key:{} already exists, it may from duplicate keys in one log expression, or memory leak. " +
                    "It's very dangerous, there must have a log expression which did not end with info(), error() and so on", key);
        }
        String valueString;
        if (value instanceof String) {
            valueString = (String) value;
        } else {
            valueString = Json.toJson(value);
        }
        contents.put(key, valueString);
        return this;
    }

    private void beforeLog() {
        Map<String, String> logContents = threadLocalContents.get();
        logContents.forEach((k, v) -> {
            originalThreadLocalContents.get().put(k, MDC.get(k));
            MDC.put(k, v);
        });
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
        MDC.put("milky_class", stackTraceElement.getClassName());
        MDC.put("milky_method", stackTraceElement.getMethodName());
        MDC.put("milky_line", Integer.toString(stackTraceElement.getLineNumber()));
        logContents.clear();
    }

    private void afterLog() {
        MortalMap<String, String> originalContents = originalThreadLocalContents.get();
        if (Collect.isNotEmpty(originalContents)) {
            originalContents.forEach(MDC::put);
            originalContents.clear();
        }
        MDC.remove("milky_class");
        MDC.remove("milky_method");
        MDC.remove("milky_line");
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
            long currentTimeMillis = Clock.currentTimeMillis();
            V result = super.put(key, value);
            LiveKey<K> liveKey = new LiveKey<>(key, currentTimeMillis + liveMillis);
            queue.remove(liveKey);
            queue.add(liveKey);
            removeDeath(currentTimeMillis);
            return result;
        }

        @Override
        public V get(Object key) {
            removeDeath(Clock.currentTimeMillis());
            return super.get(key);
        }

        @Override
        public boolean containsKey(Object key) {
            removeDeath(Clock.currentTimeMillis());
            return super.containsKey(key);
        }

        @Override
        public boolean isEmpty() {
            removeDeath(Clock.currentTimeMillis());
            return super.isEmpty();
        }

        @Override
        public boolean containsValue(Object value) {
            removeDeath(Clock.currentTimeMillis());
            return super.containsValue(value);
        }

        @Override
        public boolean replace(K key, V oldValue, V newValue) {
            removeDeath(Clock.currentTimeMillis());
            return super.replace(key, oldValue, newValue);
        }

        @Override
        public Set<K> keySet() {
            removeDeath(Clock.currentTimeMillis());
            return super.keySet();
        }

        @Override
        public int size() {
            removeDeath(Clock.currentTimeMillis());
            return super.size();
        }

        @Override
        public Collection<V> values() {
            removeDeath(Clock.currentTimeMillis());
            return super.values();
        }

        @Override
        public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
            removeDeath(Clock.currentTimeMillis());
            super.replaceAll(function);
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            removeDeath(Clock.currentTimeMillis());
            return super.entrySet();
        }

        @Override
        public V getOrDefault(Object key, V defaultValue) {
            removeDeath(Clock.currentTimeMillis());
            return super.getOrDefault(key, defaultValue);
        }

        @Override
        public V putIfAbsent(K key, V value) {
            removeDeath(Clock.currentTimeMillis());
            return super.putIfAbsent(key, value);
        }

        @Override
        public void forEach(BiConsumer<? super K, ? super V> action) {
            removeDeath(Clock.currentTimeMillis());
            super.forEach(action);
        }

        @Override
        public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
            removeDeath(Clock.currentTimeMillis());
            return super.merge(key, value, remappingFunction);
        }

        @Override
        public V replace(K key, V value) {
            removeDeath(Clock.currentTimeMillis());
            return super.replace(key, value);
        }

        @Override
        public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
            removeDeath(Clock.currentTimeMillis());
            return super.computeIfAbsent(key, mappingFunction);
        }

        @Override
        public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            removeDeath(Clock.currentTimeMillis());
            return super.computeIfPresent(key, remappingFunction);
        }

        @Override
        public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            removeDeath(Clock.currentTimeMillis());
            return super.compute(key, remappingFunction);
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> m) {
           super.putAll(m);
           long currentTimeMillis = Clock.currentTimeMillis();
           m.forEach((k, v) -> queue.add(new LiveKey<>(k, currentTimeMillis + DEFAULT_LIVE)));
           removeDeath(Clock.currentTimeMillis());
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


    @EqualsAndHashCode
    static private class LiveKey<K> implements Comparable<LiveKey<K>>{

        private final K key;

        @EqualsAndHashCode.Exclude
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
