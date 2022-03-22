package com.stellariver.milky.domain.support.base;

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
public abstract class Message {

    protected Long id;

    protected InvokeTrace invokeTrace;


    public Message(InvokeTrace invokeTrace) {
        this.id = BeanUtils.getBean(IdBuilder.class).build();
        this.invokeTrace = invokeTrace;
    }

    abstract public String getAggregateId();

}
