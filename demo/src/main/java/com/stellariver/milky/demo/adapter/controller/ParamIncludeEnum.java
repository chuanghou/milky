package com.stellariver.milky.demo.adapter.controller;

import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.exception.SysException;
import com.stellariver.milky.demo.common.enums.ChannelEnum;
import com.stellariver.milky.domain.support.ErrorEnums;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParamIncludeEnum {

    Long id;

    String name;

    String channelEnum;


    public ChannelEnum getChannelEnum() {
        return Kit.enumOf(ChannelEnum.class, channelEnum).orElseThrow(() -> new SysException(ErrorEnums.PARAM_FORMAT_WRONG));
    }

}
