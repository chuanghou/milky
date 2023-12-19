package com.stellariver.milky.common.base;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.io.Serializable;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class AbstractIterableQuery implements Serializable {

    String nextPageKey;

    @NotNull(message = "需要填写一页数量") @Positive(message = "一页数量需要大于0")
    Integer pageSize;

}
