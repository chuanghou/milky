package com.stellariver.milky.demo.domain.item.command;

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
public class ItemTitleUpdateCommand extends Command {

    Long itemId;

    String updateTitle;

    @Override
    public String getAggregateId() {
        return itemId.toString();
    }
}
