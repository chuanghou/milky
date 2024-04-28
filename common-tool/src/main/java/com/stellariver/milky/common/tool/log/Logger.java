package com.stellariver.milky.common.tool.log;

import com.stellariver.milky.common.base.BizEx;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author houchuang
 */
public class Logger implements org.slf4j.Logger {

    private final org.slf4j.Logger log;

    private final ThreadLocal<Set<String>> withKeys = ThreadLocal.withInitial(HashSet::new);

    static public Logger getLogger(Class<?> clazz) {
        return new Logger(clazz);
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

    public Logger position(String value) {
        with("position", value);
        return this;
    }

    public Logger with(String key, Object value) {
        if (key == null) {
            log.error("log key shouldn't be null");
            return this;
        }
        String oldValue = MDC.get(key);
        if (oldValue != null) {
            MDC.put("error_duplicate_key", String.format("%s:%s had been put into MDC, but not been cleared by afterLog()", key, oldValue));
        }
        withKeys.get().add(key);
        MDC.put(key, Objects.toString(value));
        return this;
    }


    public void clear() {
        withKeys.get().forEach(MDC::remove);
        withKeys.get().clear();
    }

    private void beforeLog() {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
        MDC.put("milky_class", stackTraceElement.getClassName());
        MDC.put("milky_method", stackTraceElement.getMethodName());
        MDC.put("milky_line", Integer.toString(stackTraceElement.getLineNumber()));
    }

    private void afterLog() {
        MDC.remove("milky_class");
        MDC.remove("milky_method");
        MDC.remove("milky_line");
        MDC.remove("error_duplicate_key");
        withKeys.get().forEach(MDC::remove);
        withKeys.get().clear();
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
