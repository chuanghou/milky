package com.stellariver.milky.demo.domain.inventory.event;

import com.stellariver.milky.domain.support.event.Event;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

/**
 * @author houchuang
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryCreatedEvent extends Event {

    Long itemId;

    Long initAmount;

    String storeCode;

    @Override
    public String getAggregateId() {
        return itemId.toString();
    }
}
