package com.stellariver.milky.domain.support.base;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.stellariver.milky.domain.support.dependency.IdBuilder;
import com.stellariver.milky.domain.support.invocation.InvokeTrace;
import com.stellariver.milky.domain.support.util.BeanUtil;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public abstract class Message {

    @Builder.Default
    protected Long id = BeanUtil.getBean(IdBuilder.class).build();

    protected InvokeTrace invokeTrace;

    abstract public String getAggregateId();

}
