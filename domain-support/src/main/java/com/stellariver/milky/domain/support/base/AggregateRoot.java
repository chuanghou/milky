package com.stellariver.milky.domain.support.base;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * 聚合根父类
 */
@Data
@SuperBuilder
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class AggregateRoot{

    long version;

    Map<NameType<?>, Object> metadata;

    abstract public String getAggregateId();

}
