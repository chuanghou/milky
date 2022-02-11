package com.stellariver.milky.common.tool.common;
/*
public class PrefixEnum extends Prefix {

    static public Prefix controller_monitor =  Prefix.of("controller_monitor");

}

 */
public class Prefix {

    static public Prefix ERROR_CODE = Prefix.of("ERROR_CODE");

    static public Prefix THIRD_SERVICE = Prefix.of("THIRD_SERVICE");

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
