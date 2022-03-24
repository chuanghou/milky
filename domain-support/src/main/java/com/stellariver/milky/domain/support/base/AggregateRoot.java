package com.stellariver.milky.domain.support.base;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 聚合根父类
 */
@Data
@SuperBuilder
@NoArgsConstructor
public abstract class AggregateRoot{

    abstract public String getAggregateId();

}
