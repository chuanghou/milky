package com.stellariver.milky.domain.support.base;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.stellariver.milky.common.tool.common.SystemClock;
import com.stellariver.milky.domain.support.dependency.IdBuilder;
import com.stellariver.milky.domain.support.invocation.InvokeTrace;
import com.stellariver.milky.domain.support.util.BeanUtil;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@Data
@SuperBuilder
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public abstract class Message {

    @Builder.Default
    protected Long id = BeanUtil.getBean(IdBuilder.class).build();

    @Builder.Default
    protected Date gmtCreate = SystemClock.date();

    protected InvokeTrace invokeTrace;

    abstract public String getAggregateId();

}
