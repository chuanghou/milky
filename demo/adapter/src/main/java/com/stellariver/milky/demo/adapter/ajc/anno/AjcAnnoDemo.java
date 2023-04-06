package com.stellariver.milky.demo.adapter.ajc.anno;

import com.stellariver.milky.aspectj.tool.validate.Validate;
import com.stellariver.milky.demo.adapter.ajc.Param;

import javax.validation.constraints.NotNull;

public class AjcAnnoDemo {

    @Validate
    public void testAjc(@NotNull Param str) {
    }


}