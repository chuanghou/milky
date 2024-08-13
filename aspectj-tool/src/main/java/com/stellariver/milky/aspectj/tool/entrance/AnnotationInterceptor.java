package com.stellariver.milky.aspectj.tool.entrance;

import org.aspectj.lang.ProceedingJoinPoint;

@SuppressWarnings("unused")
public interface AnnotationInterceptor {

    default void executeBefore(ProceedingJoinPoint pjp) {}


    /**
     *  should not be implemented
     */
    default void executeAfterWrapper(ProceedingJoinPoint pjp) {
        try {
            executeAfter(pjp);
        } catch (Throwable throwable) {
            logThrowable(pjp, throwable);
        }
    }

    default void executeAfter(ProceedingJoinPoint pjp) {}


    /**
     *  should not be implemented
     */
    default void afterThrowingWrapper(ProceedingJoinPoint pjp, Throwable throwable) {
        try {
            afterThrowing(pjp, throwable);
        } catch (Throwable newThrowable) {
            logThrowable(pjp, newThrowable);
        }
    }

    default void afterThrowing(ProceedingJoinPoint pjp, Throwable throwable) {}

    /**
     *  should not be implemented
     */
    default void executeFinallyWrapper(ProceedingJoinPoint pjp) {
        try {
            executeFinally(pjp);
        } catch (Throwable throwable) {
            logThrowable(pjp, throwable);
        }
    }

    default void executeFinally(ProceedingJoinPoint pjp) {}


    default boolean conditional(ProceedingJoinPoint pjp) {
        return true;
    }


    void logThrowable(ProceedingJoinPoint pjp, Throwable throwable);

}
