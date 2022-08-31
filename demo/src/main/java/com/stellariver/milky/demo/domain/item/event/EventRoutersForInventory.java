package com.stellariver.milky.demo.domain.item.event;

import com.stellariver.milky.demo.domain.inventory.event.InventoryCreatedEvent;
import com.stellariver.milky.demo.domain.inventory.event.InventoryUpdateEvent;
import com.stellariver.milky.demo.domain.item.command.ItemInventoryInitCommand;
import com.stellariver.milky.demo.domain.item.command.ItemInventoryUpdateCommand;
import com.stellariver.milky.demo.domain.service.ItemAmountUpdatedMessage;
import com.stellariver.milky.demo.domain.service.MqService;
import com.stellariver.milky.domain.support.command.CommandBus;
import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.event.EventRouter;
import com.stellariver.milky.domain.support.event.EventRouters;
import com.stellariver.milky.domain.support.event.FinalEventRouter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class EventRoutersForInventory implements EventRouters {

    final MqService mqService;


    @EventRouter
    public void amount(InventoryUpdateEvent event, Context context) {
        ItemInventoryUpdateCommand command = ItemInventoryUpdateCommand.builder()
                .itemId(event.getItemId()).amount(event.getUpdateAmount()).build();
        CommandBus.driveByEvent(command, event);
    }

    @EventRouter
    public void storeCode(InventoryCreatedEvent event, Context context) {
        ItemInventoryInitCommand command = ItemInventoryInitCommand.builder()
                .itemId(event.getItemId()).initAmount(event.getInitAmount())
                .storeCode(event.getStoreCode()).build();
        CommandBus.driveByEvent(command, event);
    }

    @FinalEventRouter
    public void mqTitleUpdated(List<ItemAmountUpdatedEvent> events, Context context) {
        events.forEach( event -> {
            ItemAmountUpdatedMessage message = ItemAmountUpdatedMessage.builder()
                    .itemId(event.getItemId())
                    .oldAmount(event.getOriginalAmount())
                    .newAmount(event.getUpdatedAmount()).build();
            mqService.sendMessage(message);
        });
    }
}
