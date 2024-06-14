package com.stellariver.milky.common.tool;

import com.google.common.util.concurrent.UncheckedExecutionException;
import lombok.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Excavator {
    public static Throwable excavate(@NonNull Throwable original) {
        Throwable current = original;
        while (true) {
            Excavate<? extends Throwable, ? extends Throwable> excavator = excavators.get(current.getClass());
            if (excavator == null) {
                return current;
            }
            Throwable throwable = excavator.excavateWrapper(current);
            if (throwable == null) {
                return current;
            } else {
                current = throwable;
            }
        }
    }


    interface Excavate<Original extends Throwable, Cause extends Throwable> {

        Cause excavate(Original original);

        @SuppressWarnings("unchecked")
        default Cause excavateWrapper(Throwable throwable) {
            return excavate((Original) throwable);
        }

    }

    static private class CauseExcavate implements Excavate<Throwable, Throwable> {

        @Override
        public Throwable excavate(Throwable e) {
            return e.getCause();
        }

    }

    static private class InvocationTargetExceptionExcavate implements Excavate<InvocationTargetException, Throwable> {

        @Override
        public Throwable excavate(InvocationTargetException e) {
            return e.getTargetException();
        }

    }

    static private final Map<Class<? extends Throwable>, Excavate<? extends Throwable, ? extends Throwable>> excavators = new HashMap<>();

    static {
        CauseExcavate causeExcavate = new CauseExcavate();
        excavators.put(UncheckedExecutionException.class, causeExcavate);
        excavators.put(ExecutionException.class, causeExcavate);
        excavators.put(UndeclaredThrowableException.class, causeExcavate);
        excavators.put(InvocationTargetException.class, new InvocationTargetExceptionExcavate());
    }


}
