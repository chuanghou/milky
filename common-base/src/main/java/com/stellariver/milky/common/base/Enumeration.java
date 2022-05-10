package com.stellariver.milky.common.base;

public class Enumeration {

    private final String code;

    private final String name;

    public Enumeration(String code, String name) {
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
