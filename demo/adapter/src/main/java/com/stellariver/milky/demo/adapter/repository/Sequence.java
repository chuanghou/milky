package com.stellariver.milky.demo.adapter.repository;

import com.stellariver.milky.common.base.OfEnum;
import com.stellariver.milky.common.base.SysEx;
import com.stellariver.milky.common.tool.common.Kit;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Sequence {

    @NotBlank
    String nameSpace;

    @NotNull
    @PositiveOrZero
    Long start;

    @NotNull
    @Min(value = 100)
    Integer step;

    @Positive
    Double alarmRatio;

    @Positive
    Long ceiling;

    @OfEnum(enumType = IdBuilder.Duty.class)
    String duty;

    public IdBuilder.Duty getDuty() {
        return Kit.enumOf(IdBuilder.Duty.class, duty).orElseThrow(SysEx::unreachable);
    }

}
