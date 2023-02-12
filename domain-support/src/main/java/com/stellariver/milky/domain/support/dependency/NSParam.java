package com.stellariver.milky.domain.support.dependency;

import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.exception.SysException;
import com.stellariver.milky.validate.tool.OfEnum;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.*;

@Data
@Builder
public class NSParam {

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
    Long end;

    @OfEnum(enumType = IdBuilder.Duty.class)
    String duty;

    public IdBuilder.Duty getDuty() {
        return Kit.enumOf(IdBuilder.Duty.class, duty).orElseThrow(SysException::unreachable);
    }

}
