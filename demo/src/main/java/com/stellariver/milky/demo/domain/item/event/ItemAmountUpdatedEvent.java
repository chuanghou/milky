package com.stellariver.milky.demo.domain.item.event;

import com.stellariver.milky.domain.support.event.Event;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;


@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemAmountUpdatedEvent extends Event {

    Long itemId;

    Long originalAmount;

    Long updatedAmount;

    @Override
    public String getAggregateId() {
        return itemId.toString();
    }

}
