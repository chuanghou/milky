package com.stellariver.milky.domain.support.event;

abstract public class ThreadLocalPasser<T> {

    abstract public T prepareThreadLocal();

    abstract public void pass(Object t);

    abstract public void clearThreadLocal();

}
