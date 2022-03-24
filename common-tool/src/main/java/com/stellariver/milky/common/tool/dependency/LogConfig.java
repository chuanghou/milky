package com.stellariver.milky.common.tool.dependency;

public interface LogConfig {

    boolean forceByNameSpace(String nameSpace);

    default boolean force() {
        return forceByNameSpace("default");
    }

}
