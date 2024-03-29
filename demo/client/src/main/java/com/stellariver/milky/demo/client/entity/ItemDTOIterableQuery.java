package com.stellariver.milky.demo.client.entity;

import com.stellariver.milky.common.base.AbstractIterableQuery;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDTOIterableQuery extends AbstractIterableQuery {

    Long userId;

}
