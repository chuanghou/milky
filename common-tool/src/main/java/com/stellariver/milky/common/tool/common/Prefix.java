package com.stellariver.milky.common.tool.common;
/*
public class PrefixEnum extends Prefix {

    static public Prefix controller_monitor =  Prefix.of("controller_monitor");

}

 */
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
