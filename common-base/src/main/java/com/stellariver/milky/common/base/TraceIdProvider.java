package com.stellariver.milky.common.base;

import javax.annotation.Nullable;

/**
 * TraceId 的生成与绑定契约，由业务方实现；未注入时使用 {@link DefaultTraceIdProvider}。
 */
public interface TraceIdProvider {

    String generateTraceId();

    @Nullable
    String getTraceId();

    void bindTraceId(String traceId);

    void unbindTraceId();

    default boolean hasTraceId() {
        String traceId = getTraceId();
        return traceId != null && !traceId.isEmpty();
    }

    /** 当前无 traceId 时生成并绑定，返回本次是否由调用方写入。 */
    default boolean bindTraceIdIfAbsent() {
        if (hasTraceId()) {
            return false;
        }
        bindTraceId(generateTraceId());
        return true;
    }

    /** 与 {@link #bindTraceIdIfAbsent()} 配对，仅清理本入口写入的 traceId。 */
    default void unbindTraceIdIfOwned(boolean boundByThisEntrance) {
        if (boundByThisEntrance) {
            unbindTraceId();
        }
    }

}
