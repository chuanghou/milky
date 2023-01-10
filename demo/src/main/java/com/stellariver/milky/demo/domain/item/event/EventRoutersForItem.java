package com.stellariver.milky.demo.domain.item.event;

import com.stellariver.milky.demo.domain.inventory.command.InventoryCreateCommand;
import com.stellariver.milky.demo.domain.service.ItemAmountUpdatedMessage;
import com.stellariver.milky.demo.domain.service.ItemCreatedMessage;
import com.stellariver.milky.demo.domain.service.ItemTitleUpdatedMessage;
import com.stellariver.milky.demo.domain.service.MqService;
import com.stellariver.milky.domain.support.command.CommandBus;
import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.event.EventRouter;
import com.stellariver.milky.domain.support.event.EventRouters;
import com.stellariver.milky.domain.support.event.FinalEventRouter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * @author houchuang
 */
@RequiredArgsConstructor
public class EventRoutersForItem implements EventRouters {

    final MqService mqService;

    @EventRouter
    public void inventory(ItemCreatedEvent event, Context context) {
        InventoryCreateCommand command = InventoryCreateCommand.builder()
                .itemId(event.getItemId()).initAmount(100L).build();
        CommandBus.driveByEvent(command, event);
    }

    @FinalEventRouter
    public void mqCreated(List<ItemCreatedEvent> events, Context context) {
        events.forEach(event -> {
            ItemCreatedMessage message = ItemCreatedMessage.builder().itemId(event.getItemId()).title(event.getTitle()).build();
            mqService.sendMessage(message);
        });
    }

    @FinalEventRouter
    public void mqForTitle(List<ItemTitleUpdatedEvent> events, Context context) {
        events.forEach(event -> {
            ItemTitleUpdatedMessage message = ItemTitleUpdatedMessage.builder()
                    .itemId(event.getItemId())
                    .oldTitle(event.getOriginalTitle())
                    .newTitle(event.getUpdatedTitle()).build();
            mqService.sendMessage(message);
        });
    }

    @FinalEventRouter
    public void mqForAmount(List<ItemAmountUpdatedEvent> events, Context context) {
        events.forEach(event -> {
            ItemAmountUpdatedMessage message = ItemAmountUpdatedMessage.builder()
                    .itemId(event.getItemId())
                    .oldAmount(event.getOriginalAmount())
                    .newAmount(event.getUpdatedAmount()).build();
            mqService.sendMessage(message);
        });
    }

}
