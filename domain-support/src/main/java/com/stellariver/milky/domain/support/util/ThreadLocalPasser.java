package com.stellariver.milky.domain.support.util;

/**
 * @author houchuang
 */
public interface ThreadLocalPasser<T> {

    T prepareThreadLocal();

    void pass(Object t);

    void clearThreadLocal();

}
