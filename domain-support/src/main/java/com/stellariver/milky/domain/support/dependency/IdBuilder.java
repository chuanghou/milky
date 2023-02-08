package com.stellariver.milky.domain.support.dependency;


/**
 * @author houchuang
 */
public interface IdBuilder {

    Long get(String nameSpace);

    void reset(String nameSpace);

    default Long get() {
        return get("default");
    }

}
