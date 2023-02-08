package com.stellariver.milky.domain.support.dependency;


import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.exception.SysException;
import com.stellariver.milky.validate.tool.OfEnum;
import lombok.Data;

import javax.annotation.Nullable;
import javax.validation.constraints.*;

@Data
public class NameSpaceParam {

    @NotBlank
    String nameSpace;

    @NotNull
    @PositiveOrZero
    Long start;

    @NotNull
    @Min(value = 100)
    Integer step;

    @OfEnum(enumType = IdBuilder.Duty.class)
    String duty;

    public IdBuilder.Duty getDuty() {
        return Kit.enumOf(IdBuilder.Duty.class, duty).orElseThrow(SysException::unreachable);
    }

}
