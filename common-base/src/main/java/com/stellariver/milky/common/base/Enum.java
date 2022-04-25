package com.stellariver.milky.common.base;

public class Enum {

    private final String code;

    private final String name;

    public Enum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
