package com.stellariver.milky.aspectj.tool.log;

public class LogConfig {

    private Boolean debug = false;

    private Boolean useMDC = false;

    static private final LogConfig DEFAULT_INSTANCE = new LogConfig();

    static public LogConfig defaultConfig() {
        return DEFAULT_INSTANCE;
    }

    public Boolean getDebug() {
        return debug;
    }

    public void setDebug(Boolean debug) {
        this.debug = debug;
    }

    public Boolean getUseMDC() {
        return useMDC;
    }

    public void setUseMDC(Boolean useMDC) {
        this.useMDC = useMDC;
    }
}
