package com.stellariver.milky.domain.support.event;

abstract public class ThreadLocalTransfer<T> {

    abstract public T prepareThreadLocal();

    abstract public void fillThreadLocal(Object t);

    abstract public void clearThreadLocal();

}
