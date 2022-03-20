package com.stellariver.milky.common.tool.common;

public class Prefix {

    private String preFix;

    public String getPreFix() {
        return preFix;
    }

    private Prefix(String preFix) {
        this.preFix = preFix;
    }

    protected Prefix() {
    }

    static public Prefix of(String preFix) {
        return new Prefix(preFix);
    }
}
