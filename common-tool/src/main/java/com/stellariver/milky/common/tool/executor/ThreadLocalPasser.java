package com.stellariver.milky.common.tool.executor;

/**
 * @author houchuang
 * @see TraceIdThreadLocalPasser
 */
public interface ThreadLocalPasser<T> {

    T prepareThreadLocal();

    void pass(Object t);

    void clearThreadLocal();

}
