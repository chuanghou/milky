package com.stellariver.milky.demo.adapter.ajc.anno;

import com.stellariver.milky.aspectj.tool.log.Log;
import com.stellariver.milky.aspectj.tool.validate.Validate;
import com.stellariver.milky.demo.adapter.ajc.Param;

import jakarta.validation.constraints.NotNull;

public class AjcAnnoDemo {

    @Validate
    public void testAjc(@NotNull Param str) {
    }


    @Log
    public Object testAjcLogOfAnno(String para1, Long para2) {
        return "ok";
    }

}