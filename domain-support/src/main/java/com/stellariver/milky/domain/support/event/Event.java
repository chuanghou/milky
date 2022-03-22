package com.stellariver.milky.domain.support.event;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.stellariver.milky.domain.support.base.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * 领域事件的抽象父类
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public abstract class Event extends Message {

    private boolean aggregateChange = false;

    public Event() {
        super();
    }

    public Event(boolean aggregateChange) {
        super();
        this.aggregateChange = aggregateChange;
    }

}
