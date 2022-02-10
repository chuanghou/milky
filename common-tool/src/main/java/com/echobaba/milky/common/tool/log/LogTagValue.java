package com.echobaba.milky.common.tool.log;


import com.echobaba.milky.client.base.Prefix;

/*
public class LogTagValueEnum extends LogTagValue{

    static public final LogTagValue success = LogTagValue.of("success");

    static public final LogTagValue failure = LogTagValue.of("failure");

}
 */
public class LogTagValue {

    static public final LogTagValue success = LogTagValue.of("success");

    static public final LogTagValue failure = LogTagValue.of("failure");

    static public final LogTagValue unknown = LogTagValue.of("unknown");

    static public final LogTagValue fatalError = LogTagValue.of("fatalError");

    private String value;

    private LogTagValue(String value) {
        this.value = value;
    }

    protected LogTagValue(){}

    public String getValue() {
        return value;
    }

    static public LogTagValue of(Prefix prefix, String value) {
        return new LogTagValue(prefix.getPreFix() + value);
    }

    static protected LogTagValue of(String value) {
        return new LogTagValue(value);
    }
}
