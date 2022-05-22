package com.stellariver.milky.common.base;

public class Enumeration {

    private final String id;

    private final String name;

    public Enumeration(String code, String name) {
        this.id = code;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
