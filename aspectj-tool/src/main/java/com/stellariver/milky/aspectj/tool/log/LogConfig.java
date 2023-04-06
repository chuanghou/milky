package com.stellariver.milky.aspectj.tool.log;

public class LogConfig {

    private boolean debug = false;

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    static private final LogConfig DEFAULT_INSTANCE = new LogConfig();

    static public LogConfig defaultConfig() {
        return DEFAULT_INSTANCE;
    }

}
