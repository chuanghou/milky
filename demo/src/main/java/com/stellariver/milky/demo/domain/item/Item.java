package com.stellariver.milky.demo.domain.item;

import com.stellariver.milky.demo.basic.NameTypes;
import com.stellariver.milky.demo.domain.item.command.ItemInventoryUpdateCommand;
import com.stellariver.milky.demo.domain.item.command.ItemCreateCommand;
import com.stellariver.milky.demo.domain.item.command.ItemInventoryInitCommand;
import com.stellariver.milky.demo.domain.item.command.ItemTitleUpdateCommand;
import com.stellariver.milky.demo.domain.item.dependency.UserInfo;
import com.stellariver.milky.demo.domain.item.event.ItemAmountUpdatedEvent;
import com.stellariver.milky.demo.domain.item.event.ItemCreatedEvent;
import com.stellariver.milky.demo.domain.item.event.ItemInventoryInitEvent;
import com.stellariver.milky.demo.domain.item.event.ItemTitleUpdatedEvent;
import com.stellariver.milky.domain.support.base.AggregateRoot;
import com.stellariver.milky.domain.support.command.CommandHandler;
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
public class Item extends AggregateRoot {

    Long itemId;

    String title;

    Long userId;

    String userName;

    Long amount;

    String storeCode;

    @CommandHandler(dependencies = "userInfo")
    public Item(ItemCreateCommand command, Context context) {
        this.itemId = command.getItemId();
        this.title = command.getTitle();
        this.userId = command.getUserId();
        UserInfo userInfo = NameTypes.userInfo.extractFrom(context.getDependencies());
        this.userName = userInfo.getUserName();
        context.publish(ItemCreatedEvent.builder().itemId(itemId).title(title).build());
    }

    @CommandHandler
    public void handle(ItemTitleUpdateCommand command, Context context) {
        String originalTitle = this.title;
        this.title = command.getUpdateTitle();
        ItemTitleUpdatedEvent event = ItemTitleUpdatedEvent.builder()
                .itemId(itemId).originalTitle(originalTitle).updatedTitle(title).build();
        context.publish(event);
    }

    @CommandHandler
    public void handle(ItemInventoryUpdateCommand command, Context context) {
        Long originalAmount = this.amount;
        this.amount = command.getAmount();
        ItemAmountUpdatedEvent event = ItemAmountUpdatedEvent.builder()
                .itemId(itemId).originalAmount(originalAmount).updatedAmount(amount).build();
        context.publish(event);
    }

    @CommandHandler
    public void handle(ItemInventoryInitCommand command, Context context) {
        ItemInventoryInitEvent event = ItemInventoryInitEvent.builder()
                .itemId(itemId).initStoreCode(this.storeCode).initAmount(command.getInitAmount()).build();
        this.amount = command.getInitAmount();
        this.storeCode = command.getStoreCode();
        context.publish(event);
    }

    @Override
    public String getAggregateId() {
        return itemId.toString();
    }
}
