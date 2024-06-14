package com.stellariver.milky.common.tool;

import lombok.NonNull;

import java.lang.reflect.InvocationTargetException;

public class Excavator {
    public static Throwable excavate(@NonNull Throwable original) {
        Throwable current = original;
        Throwable next;
        while (true) {
            if ((original instanceof InvocationTargetException)) {
                next = ((InvocationTargetException) current).getTargetException();
            } else {
                next = current.getCause();
            }

            if (next == null) {
                return current;
            } else {
                current = next;
            }
        }
    }



}
