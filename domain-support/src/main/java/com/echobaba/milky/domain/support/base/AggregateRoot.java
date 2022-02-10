package com.echobaba.milky.domain.support.base;

/**
 * 聚合根父类
 */

public abstract class AggregateRoot{

    abstract public String getAggregateId();

    abstract public void setAggregateId(String aggregateId);
}
