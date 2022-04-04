package com.stellariver.milky.demo.domain.item;

import com.stellariver.milky.demo.domain.item.command.ItemCreateCommand;
import com.stellariver.milky.demo.domain.item.event.ItemCreatedEvent;
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

    @CommandHandler
    public Item(ItemCreateCommand command, Context context) {
        this.itemId = command.getItemId();
        this.title = command.getTitle();
        context.publish(ItemCreatedEvent.builder().itemId(itemId).build());
    }

    @Override
    public String getAggregateId() {
        return itemId.toString();
    }
}
