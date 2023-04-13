package com.stellariver.milky.domain.support.base;

import com.stellariver.milky.common.tool.common.Typed;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * 聚合根父类
 * @author houchuang
 */
@Data
@SuperBuilder
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class AggregateRoot{

    long version;

    Map<Typed<?>, Object> metadata;

    abstract public String getAggregateId();



}
