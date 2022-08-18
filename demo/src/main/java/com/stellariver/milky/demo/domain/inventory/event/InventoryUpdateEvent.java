package com.stellariver.milky.demo.domain.inventory.event;

import com.stellariver.milky.domain.support.event.Event;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;


@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryUpdateEvent extends Event {

    Long itemId;

    Long originalAmount;

    Long updateAmount;


    @Override
    public String getAggregateId() {
        return itemId.toString();
    }
}
