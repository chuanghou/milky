package com.stellariver.milky.common.tool;

import com.google.common.util.concurrent.UncheckedExecutionException;
import lombok.NonNull;

import java.lang.reflect.InvocationTargetException;
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

    static private class InvocationTargetExceptionExcavate implements Excavate<InvocationTargetException, Throwable> {

        @Override
        public Throwable excavate(InvocationTargetException e) {
            return e.getTargetException();
        }

    }


    static private class UncheckedExecutionExceptionExcavate implements Excavate<UncheckedExecutionException, Throwable> {

        @Override
        public Throwable excavate(UncheckedExecutionException e) {
            return e.getCause();
        }

    }

    static private class ExecutionExceptionExcavate implements Excavate<ExecutionException, Throwable> {

        @Override
        public Throwable excavate(ExecutionException e) {
            return e.getCause();
        }

    }

    static private final Map<Class<? extends Throwable>, Excavate<? extends Throwable, ? extends Throwable>> excavators = new HashMap<>();

    static {
        excavators.put(UncheckedExecutionException.class, new UncheckedExecutionExceptionExcavate());
        excavators.put(ExecutionException.class, new ExecutionExceptionExcavate());
        excavators.put(InvocationTargetException.class, new InvocationTargetExceptionExcavate());
    }


}
