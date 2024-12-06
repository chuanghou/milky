package com.stellariver.milky.common.base;

import org.slf4j.MDC;

import javax.annotation.Nullable;
import java.util.UUID;

public class TraceIdContext {

    private volatile static TraceIdContext instance = null;

    public static TraceIdContext getInstance() {
        if (instance == null) {
            throw new IllegalStateException("TraceIdContext not initialized");
        }
        return instance;
    }

    public static void init(@Nullable TraceIdContext externalInstance) {

        if (instance == null) {
            synchronized (TraceIdContext.class) {
                if (instance == null) {
                    if (externalInstance == null) {
                        instance = new TraceIdContext();
                    } else  {
                        instance = externalInstance;
                    }
                }
            }
        }
    }


    public String buildTraceId() {
        return UUID.randomUUID().toString();
    }

    public void storeTraceId(String traceId) {
        MDC.put("traceId", traceId);
    }

    public void removeTraceId() {
        MDC.remove("traceId");
    }

    public String getTraceId() {
        return MDC.get("traceId");
    }


}
