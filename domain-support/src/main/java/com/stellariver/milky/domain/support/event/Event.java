package com.stellariver.milky.domain.support.event;

import com.stellariver.milky.domain.support.base.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 领域事件的抽象父类
 * @author houchuang
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class Event extends Message {

    public boolean aggregateChanged() {
        return true;
    }

}
