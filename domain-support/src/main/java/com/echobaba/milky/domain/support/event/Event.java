package com.echobaba.milky.domain.support.event;

import com.alibaba.c2m.milky.domain.support.base.Message;
import lombok.*;

/**
 * 领域事件的抽象父类
 */

public abstract class Event extends Message {

    public Event() {
        super();
    }

}
