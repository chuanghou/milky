package com.stellariver.milky.demo.domain.item;

import com.stellariver.milky.demo.domain.item.command.ItemCreateCommand;
import com.stellariver.milky.demo.domain.item.command.ItemUpdateCommand;
import com.stellariver.milky.demo.domain.item.event.ItemCreatedEvent;
import com.stellariver.milky.demo.domain.item.event.ItemTitleChangedEvent;
import com.stellariver.milky.domain.support.base.AggregateRoot;
import com.stellariver.milky.domain.support.command.CommandHandler;
import com.stellariver.milky.domain.support.context.Context;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Item extends AggregateRoot {

    Long itemId;

    String title;

    Long sellerId;

    String userName;

    @CommandHandler(dependencies = "userName")
    public Item(ItemCreateCommand command, Context context) {
        this.itemId = command.getItemId();
        this.title = command.getTitle();
        this.sellerId = command.getSellerId();
        context.publish(ItemCreatedEvent.builder().itemId(itemId).build());
    }

    @CommandHandler
    public void handle(ItemUpdateCommand command, Context context) {
        String originalTitle = this.title;
        this.title = command.getNewTitle();
        ItemTitleChangedEvent event = ItemTitleChangedEvent.builder()
                .itemId(itemId).oldTitle(originalTitle).newTitle(this.title).build();
        context.publish(event);
    }

    @Override
    public String getAggregateId() {
        return itemId.toString();
    }
}
