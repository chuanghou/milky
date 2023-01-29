package com.stellariver.milky.demo;

import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.demo.basic.ChannelEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EnumTest {

    @Test
    public void enumOfTest() {
        ChannelEnum channelEnum = Kit.enumOf(ChannelEnum::getDisplay, "阿里");
        Assertions.assertEquals(channelEnum, ChannelEnum.ALI);
    }

}
