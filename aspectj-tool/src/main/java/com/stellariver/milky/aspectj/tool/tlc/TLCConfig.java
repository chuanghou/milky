package com.stellariver.milky.aspectj.tool.tlc;

import com.stellariver.milky.common.tool.common.BaseQuery;

import java.util.HashSet;
import java.util.Set;

public class TLCConfig {

    private Set<Class<? extends BaseQuery<?, ?>>> threadLocalBaseQueryTypes = new HashSet<>();

    public Set<Class<? extends BaseQuery<?, ?>>> getThreadLocalBaseQueryTypes() {
        return threadLocalBaseQueryTypes;
    }

    public void setThreadLocalBaseQueryTypes(Set<Class<? extends BaseQuery<?, ?>>> threadLocalBaseQueryTypes) {
        this.threadLocalBaseQueryTypes = threadLocalBaseQueryTypes;
    }

    static private final TLCConfig DEFAULT_INSTANCE = new TLCConfig();

    static public TLCConfig defaultConfig() {
        return DEFAULT_INSTANCE;
    }

}
