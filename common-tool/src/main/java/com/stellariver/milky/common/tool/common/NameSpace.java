package com.stellariver.milky.common.tool.common;

public class NameSpace {

    final String name;

    public NameSpace(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    static public NameSpace build(Class<?> clazz) {
        return new NameSpace(clazz.getName());
    }

}
