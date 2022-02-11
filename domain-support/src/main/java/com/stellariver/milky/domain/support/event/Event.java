package com.stellariver.milky.domain.support.event;

import com.stellariver.milky.domain.support.base.Message;
import lombok.experimental.SuperBuilder;

/**
 * 领域事件的抽象父类
 */
@SuperBuilder
public abstract class Event extends Message {

    public Event() {
        super();
    }

}
