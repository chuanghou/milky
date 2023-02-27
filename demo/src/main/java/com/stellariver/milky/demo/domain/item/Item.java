package com.stellariver.milky.demo.domain.item;

import com.stellariver.milky.common.tool.common.Clock;
import com.stellariver.milky.demo.common.enums.ChannelEnum;
import com.stellariver.milky.demo.domain.item.command.ItemInventoryUpdateCommand;
import com.stellariver.milky.demo.domain.item.command.ItemCreateCommand;
import com.stellariver.milky.demo.domain.item.command.ItemInventoryInitCommand;
import com.stellariver.milky.demo.domain.item.command.ItemTitleUpdateCommand;
import com.stellariver.milky.demo.domain.item.dependency.UserInfo;
import com.stellariver.milky.demo.domain.item.event.ItemAmountUpdatedEvent;
import com.stellariver.milky.demo.domain.item.event.ItemCreatedEvent;
import com.stellariver.milky.demo.domain.item.event.ItemInventoryInitEvent;
import com.stellariver.milky.demo.domain.item.event.ItemTitleUpdatedEvent;
import com.stellariver.milky.demo.domain.item.repository.UserInfoRepository;
import com.stellariver.milky.domain.support.base.AggregateRoot;
import com.stellariver.milky.domain.support.command.MethodHandler;
import com.stellariver.milky.domain.support.command.ConstructorHandler;
import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.util.BeanUtil;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;


import static com.stellariver.milky.demo.basic.TypedEnums.*;

/**
 * @author houchuang
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unused")
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Item extends AggregateRoot {

    Long itemId;
    String title;
    Long userId;
    String userName;
    Long amount;
    String storeCode;
    ChannelEnum channelEnum;

    protected Item(ItemCreateCommand command) {
        this.itemId = command.getItemId();
        this.title = command.getTitle();
        this.userId = command.getUserId();
        this.amount = command.getAmount();
        this.storeCode = command.getStoreCode();
        this.channelEnum = command.getChannelEnum();
    }

    @ConstructorHandler()
    static public Item build(ItemCreateCommand command, Context context) {
        Item item = new Item(command);
        context.publish(ItemCreatedEvent.builder().itemId(item.getItemId()).title(item.getTitle()).build());
        UserInfo userInfo = context.proxy(UserInfoRepository.class, USER_INFO.class).getUserInfo(command.getUserId());
        item.setUserName(userInfo.getUserName());
        return item;
    }

    @SneakyThrows
    @MethodHandler
    public Context handle(ItemTitleUpdateCommand command, Context context) {
        String originalTitle = this.title;
        this.title = command.getUpdateTitle();
        ItemTitleUpdatedEvent event = ItemTitleUpdatedEvent.builder()
                .itemId(itemId).originalTitle(originalTitle).updatedTitle(title).build();
        context.addMetaData(MARK_HANDLE.class, Clock.currentTimeMillis());
        Thread.sleep(10L);
        context.publish(event);
        return context;
    }

    @MethodHandler
    public void handle(ItemInventoryUpdateCommand command, Context context) {
        Long originalAmount = this.amount;
        this.amount = command.getAmount();
        ItemAmountUpdatedEvent event = ItemAmountUpdatedEvent.builder()
                .itemId(itemId).originalAmount(originalAmount).updatedAmount(amount).build();
        context.publish(event);
    }

    @MethodHandler
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
