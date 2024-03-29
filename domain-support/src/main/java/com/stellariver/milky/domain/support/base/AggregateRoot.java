package com.stellariver.milky.domain.support.base;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

/**
 * 聚合根父类
 * @author houchuang
 */
@Data
@SuperBuilder
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class AggregateRoot{

    Integer version = 0;

    abstract public String getAggregateId();

}
