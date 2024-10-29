package com.stellariver.milky.common.base;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.io.Serializable;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class AbstractPageQuery implements Serializable {

    @NotNull(message = "分页查询页码必传")
    @Positive(message = "页面必须大于，从1开始")
    Integer pageIndex;

    @NotNull(message = "页面条数必传")
    @Positive(message = "页面条数必为正数")
    Integer pageSize;

}
