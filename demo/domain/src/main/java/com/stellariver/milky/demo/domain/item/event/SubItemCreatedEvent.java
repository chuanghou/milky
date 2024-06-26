package com.stellariver.milky.demo.domain.item.event;

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
public class SubItemCreatedEvent extends ItemCreatedEvent {

    String name;

}
