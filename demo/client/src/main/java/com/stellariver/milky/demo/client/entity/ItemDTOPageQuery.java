package com.stellariver.milky.demo.client.entity;

import com.stellariver.milky.common.base.AbstractPageQuery;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDTOPageQuery extends AbstractPageQuery {

    @NotNull
    Long userId;

}
