package com.stellariver.milky.common.tool;

import com.google.common.util.concurrent.UncheckedExecutionException;
import com.stellariver.milky.common.base.BaseEx;
import lombok.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

/**
 * 沿 cause 链剥离常见反射/并发包装，保留 {@link BaseEx} 与 {@link Error} 不再向下剥。
 */
public class Excavator {

    public static Throwable excavate(@NonNull Throwable original) {
        Throwable current = original;
        while (true) {
            if (current instanceof BaseEx || current instanceof Error) {
                return current;
            }
            Throwable next = unwrapOne(current);
            if (next == null) {
                return current;
            }
            current = next;
        }
    }

    private static Throwable unwrapOne(Throwable current) {
        if (current instanceof InvocationTargetException) {
            return ((InvocationTargetException) current).getTargetException();
        }
        if (current instanceof UndeclaredThrowableException) {
            return current.getCause();
        }
        if (current instanceof UncheckedExecutionException
                || current instanceof ExecutionException
                || current instanceof CompletionException) {
            return current.getCause();
        }
        return null;
    }

}
