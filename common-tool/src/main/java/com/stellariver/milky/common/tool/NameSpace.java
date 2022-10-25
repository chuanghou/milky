package com.stellariver.milky.common.tool;

public class NameSpace {

    final String name;

    public NameSpace(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String preFix(String delimiter, String value) {
        return name + "_" + value;
    }

    public String preFix(String value) {
        return preFix("_", value);
    }

    static public NameSpace build(Class<?> clazz) {
        return new NameSpace(clazz.getName());
    }

}
