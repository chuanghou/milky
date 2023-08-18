package com.stellariver.milky.common.tool.executor;

/**
 * @author houchuang
 */
public interface ThreadLocalPasser<T> {

    T prepareThreadLocal();

    void pass(Object t);

    void clearThreadLocal();

}
