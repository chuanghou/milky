package com.stellariver.milky.demo.domain.item.command;

import com.stellariver.milky.domain.support.command.Command;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

/**
 * @author houchuang
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemInventoryUpdateCommand extends Command {

    Long itemId;

    Long amount;

    @Override
    public String getAggregateId() {
        return itemId.toString();
    }
}
