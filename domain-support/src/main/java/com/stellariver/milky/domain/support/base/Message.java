package com.stellariver.milky.domain.support.base;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.stellariver.milky.common.tool.common.Clock;
import com.stellariver.milky.domain.support.dependency.UniqueIdGetter;
import com.stellariver.milky.domain.support.invocation.InvokeTrace;
import com.stellariver.milky.common.tool.common.BeanUtil;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;

/**
 * @author houchuang
 */
@Data
@SuperBuilder
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public abstract class Message {

    @Builder.Default
    protected Long id = BeanUtil.getBean(UniqueIdGetter.class).get();

    @Builder.Default
    protected Date gmtCreate = Clock.now();

    protected InvokeTrace invokeTrace;

    abstract public String getAggregateId();

}
