package com.stellariver.milky.demo.adapter.ajc.custom;

import com.stellariver.milky.demo.adapter.ajc.Param;

import javax.validation.constraints.NotNull;

public class AjcCustomDemo {

    @NotNull
    public Object testAjc(@NotNull Param str) {
        return null;
    }

    public Object testAjcLog(String para1, Long para2) {
        return "ok";
    }

}