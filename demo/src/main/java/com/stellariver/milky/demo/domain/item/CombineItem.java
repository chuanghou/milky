package com.stellariver.milky.demo.domain.item;

import com.stellariver.milky.demo.domain.item.command.*;
import com.stellariver.milky.demo.domain.item.event.*;
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
public class CombineItem extends Item {

    Long ratio;

    protected CombineItem(CombineItemCreateCommand command) {
        super(command);
        this.ratio = command.getRatio();
    }

    @ConstructorHandler
    static public CombineItem build(CombineItemCreateCommand command, Context context) {
        CombineItem combineItem = new CombineItem(command);
        CombineItemCreatedEvent event = CombineItemCreatedEvent.builder()
                .itemId(combineItem.getItemId())
                .title(combineItem.getTitle())
                .build();
        context.publish(event);
        return combineItem;
    }

    @Override
    public String getAggregateId() {
        return super.getAggregateId();
    }
}
