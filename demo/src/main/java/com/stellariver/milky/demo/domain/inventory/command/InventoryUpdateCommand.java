package com.stellariver.milky.demo.domain.inventory.command;

import com.stellariver.milky.domain.support.command.Command;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryUpdateCommand extends Command {

    Long itemId;

    Long updateAmount;

    @Override
    public String getAggregateId() {
        return itemId.toString();
    }

}
