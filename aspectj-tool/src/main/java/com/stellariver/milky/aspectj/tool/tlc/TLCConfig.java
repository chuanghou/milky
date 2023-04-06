package com.stellariver.milky.aspectj.tool.tlc;

import com.stellariver.milky.common.tool.common.BaseQuery;

import java.util.HashSet;
import java.util.Set;

public class TLCConfig {

    private Set<Class<? extends BaseQuery<?, ?>>> disableBaseQueries = new HashSet<>();

    public Set<Class<? extends BaseQuery<?, ?>>> getDisableBaseQueries() {
        return disableBaseQueries;
    }

    public void setDisableBaseQueries(Set<Class<? extends BaseQuery<?, ?>>> disableBaseQueries) {
        this.disableBaseQueries = disableBaseQueries;
    }

    static private final TLCConfig DEFAULT_INSTANCE = new TLCConfig();

    static public TLCConfig defaultConfig() {
        return DEFAULT_INSTANCE;
    }

}
