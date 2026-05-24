package com.stellariver.milky.common.base;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * 默认实现：UUID 生成，通过 SLF4J {@link MDC} 存取 {@value #MDC_KEY}。
 */
public class DefaultTraceIdProvider implements TraceIdProvider {

    public static final String MDC_KEY = "traceId";

    @Override
    public String generateTraceId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public String getTraceId() {
        return MDC.get(MDC_KEY);
    }

    @Override
    public void bindTraceId(String traceId) {
        MDC.put(MDC_KEY, traceId);
    }

    @Override
    public void unbindTraceId() {
        MDC.remove(MDC_KEY);
    }

}
