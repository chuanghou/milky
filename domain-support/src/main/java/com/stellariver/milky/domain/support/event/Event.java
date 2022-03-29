package com.stellariver.milky.domain.support.event;

import com.stellariver.milky.domain.support.base.Message;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * 领域事件的抽象父类
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public abstract class Event extends Message {

    @Builder.Default
    private boolean aggregateChange = true;

}
