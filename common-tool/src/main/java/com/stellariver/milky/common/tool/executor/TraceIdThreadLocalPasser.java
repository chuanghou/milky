package com.stellariver.milky.common.tool.executor;

import org.slf4j.MDC;

public class TraceIdThreadLocalPasser implements ThreadLocalPasser<String>{

    @Override
    public String prepareThreadLocal() {
        return MDC.get("traceId");
    }

    @Override
    public void pass(Object t) {
        MDC.put("traceId", t.toString());
    }

    @Override
    public void clearThreadLocal() {
        MDC.remove("traceId");
    }

}
