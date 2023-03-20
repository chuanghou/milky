package com.stellariver.milky.domain.support.dependency;

import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.exception.SysEx;
import com.stellariver.milky.common.tool.validate.OfEnum;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.*;

@Data
@Builder
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
