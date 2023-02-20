package com.stellariver.milky.demo.domain.inventory;

import com.stellariver.milky.demo.domain.inventory.command.InventoryCreateCommand;
import com.stellariver.milky.demo.domain.inventory.command.InventoryUpdateCommand;
import com.stellariver.milky.demo.domain.inventory.event.InventoryCreatedEvent;
import com.stellariver.milky.demo.domain.inventory.event.InventoryUpdateEvent;
import com.stellariver.milky.domain.support.base.AggregateRoot;
import com.stellariver.milky.domain.support.command.MethodHandler;
import com.stellariver.milky.domain.support.command.ConstructorHandler;
import com.stellariver.milky.domain.support.context.Context;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

/**
 * @author houchuang
 */
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

    protected Inventory(InventoryCreateCommand command, Context context) {
        this.itemId = command.getItemId();
        this.amount = command.getInitAmount();
        this.storeCode = "jd";
    }

    @ConstructorHandler
    public static Inventory build(InventoryCreateCommand command, Context context) {
        Inventory inventory = new Inventory(command, context);
        InventoryCreatedEvent event = InventoryCreatedEvent.builder()
                .itemId(inventory.getItemId()).initAmount(inventory.getAmount()).storeCode(inventory.getStoreCode())
                .build();
        context.publish(event);
        return inventory;
    }

    @MethodHandler
    public void handleInventoryUpdateCommand(InventoryUpdateCommand command, Context context) {
        Long originalAmount = this.amount;
        this.amount = command.getUpdateAmount();
        InventoryUpdateEvent event = InventoryUpdateEvent.builder().itemId(itemId)
                .originalAmount(originalAmount).updateAmount(amount).build();
        context.publish(event);
    }

}
