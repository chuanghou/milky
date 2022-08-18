package com.stellariver.milky.demo.domain.inventory;

import com.stellariver.milky.demo.domain.inventory.command.InventoryCreateCommand;
import com.stellariver.milky.domain.support.base.AggregateRoot;
import com.stellariver.milky.domain.support.context.Context;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Inventory extends AggregateRoot {

    private Long itemId;

    private Long amount;

    private String storeCode;

    @Override
    public String getAggregateId() {
        return itemId.toString();
    }

    public Inventory(InventoryCreateCommand command, Context context) {
        this.itemId = command.getItemId();
        this.amount = command.getInitAmount();
        this.storeCode = "jd";

    }


}
