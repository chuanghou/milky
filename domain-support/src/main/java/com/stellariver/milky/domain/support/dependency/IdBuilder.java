package com.stellariver.milky.domain.support.dependency;


/**
 * @author houchuang
 */
public interface IdBuilder {

    Long build(String nameSpace);

    default Long build() {
        return build("default");
    }

}
