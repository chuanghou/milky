package com.stellariver.milky.domain.support.base;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.stellariver.milky.domain.support.IdBuilder;
import com.stellariver.milky.domain.support.InvokeTrace;
import com.stellariver.milky.common.tool.util.BeanUtils;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public abstract class Message {

    @Builder.Default
    protected Long id = BeanUtils.getBean(IdBuilder.class).build();

    protected InvokeTrace invokeTrace;

    abstract public String getAggregateId();

}
