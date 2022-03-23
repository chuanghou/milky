package com.stellariver.milky.domain.support.base;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.stellariver.milky.domain.support.IdBuilder;
import com.stellariver.milky.domain.support.Invocation;
import com.stellariver.milky.domain.support.InvokeTrace;
import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.util.BeanUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@SuperBuilder
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public abstract class Message {

    protected Long id;

    protected String aggregateId;

    protected InvokeTrace invokeTrace;

    public Message(String aggregateId, InvokeTrace invokeTrace) {
        this.id = BeanUtils.getBean(IdBuilder.class).build();
        this.aggregateId = aggregateId;
        this.invokeTrace = invokeTrace;
    }

    public String getAggregateId() {
        return aggregateId;
    }

}
