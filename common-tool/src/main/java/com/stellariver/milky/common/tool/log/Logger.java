package com.stellariver.milky.common.tool.log;

import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class Logger implements org.slf4j.Logger {

    private final org.slf4j.Logger log;

    private final ThreadLocal<MortalMap<String, String>> threadLocalContents = new ThreadLocal<>();

    private final ThreadLocal<MortalMap<String, String>> tempThreadLocalContents = new ThreadLocal<>();

    static public Logger getLogger(Class<?> clazz) {
        return new Logger(clazz);
    }

    private Logger(Class<?> clazz){
        this.log = LoggerFactory.getLogger(clazz);
    }

    public Logger logTag(Object value) {
        return with("logTag", value);
    }

    public Logger with(String key, Object value) {
        if (key == null) {
            log.error("log key shouldn't be null");
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
        value = Optional.ofNullable(value).orElse("null");
        contents.put(key, value.toString());
        return this;
    }

    private void beforeLog() {
        Map<String, String> logContents = threadLocalContents.get();
        if (logContents == null || logContents.isEmpty()) {
            // 如果没有没有存储任何信息，那就直接返回
            return;
        }
        if (tempThreadLocalContents.get() == null) {
            tempThreadLocalContents.set(new MortalMap<>());
        }
        logContents.forEach((k, v) -> {
            String originalValue = MDC.get(k);
            tempThreadLocalContents.get().put(k, originalValue);
            MDC.put(k, v);
        });

        logContents.clear();
    }

    private void afterLog() {
        tempThreadLocalContents.get().forEach(MDC::put);
        tempThreadLocalContents.get().clear();
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
}
