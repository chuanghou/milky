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
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public abstract class Message {

    @Builder.Default
    protected Long id = BeanUtils.getBean(IdBuilder.class).build();

    protected InvokeTrace invokeTrace;

    abstract public String getAggregateId();

}
