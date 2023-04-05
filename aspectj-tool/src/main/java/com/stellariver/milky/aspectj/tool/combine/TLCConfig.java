package com.stellariver.milky.aspectj.tool.combine;

import com.stellariver.milky.common.tool.common.BaseQuery;

public class TLCConfig {

    Class<? extends BaseQuery<?, ?>>[] disableBaseQueries = new Class[0];

    public Class<? extends BaseQuery<?, ?>>[] getDisableBaseQueries() {
        return disableBaseQueries;
    }

    public void setDisableBaseQueries(Class<? extends BaseQuery<?, ?>>[] disableBaseQueries) {
        this.disableBaseQueries = disableBaseQueries;
    }

    static private final TLCConfig DEFAULT_INSTANCE = new TLCConfig();

    static public TLCConfig defaultConfig() {
        return DEFAULT_INSTANCE;
    }

}
