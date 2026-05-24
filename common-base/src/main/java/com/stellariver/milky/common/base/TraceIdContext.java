package com.stellariver.milky.common.base;

import javax.annotation.Nullable;

/**
 * TraceId 注册入口：{@link #init(TraceIdProvider)} 注入外部实现，否则使用 {@link DefaultTraceIdProvider}。
 */
public final class TraceIdContext {

    private static volatile TraceIdProvider provider;

    private static final Object LOCK = new Object();

    private TraceIdContext() {
    }

    public static boolean isInitialized() {
        return provider != null;
    }

    public static void init(@Nullable TraceIdProvider externalProvider) {
        if (provider != null) {
            return;
        }
        synchronized (LOCK) {
            if (provider == null) {
                provider = externalProvider != null ? externalProvider : new DefaultTraceIdProvider();
            }
        }
    }

    public static TraceIdProvider getProvider() {
        return resolve();
    }

    public static boolean ensureTraceIdInMdc() {
        return resolve().bindTraceIdIfAbsent();
    }

    public static void clearEntranceTraceId(boolean boundByThisEntrance) {
        resolve().unbindTraceIdIfOwned(boundByThisEntrance);
    }

    private static TraceIdProvider resolve() {
        TraceIdProvider current = provider;
        if (current != null) {
            return current;
        }
        synchronized (LOCK) {
            if (provider == null) {
                provider = new DefaultTraceIdProvider();
            }
            return provider;
        }
    }

}
