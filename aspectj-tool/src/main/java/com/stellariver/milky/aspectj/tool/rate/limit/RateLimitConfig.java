package com.stellariver.milky.aspectj.tool.rate.limit;

public class RateLimitConfig {

    static private final RateLimitConfig DEFAULT_INSTANCE = new RateLimitConfig();

    static public RateLimitConfig defaultConfig() {
        return DEFAULT_INSTANCE;
    }

}
