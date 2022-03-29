package com.stellariver.milky.domain.support.dependency;

public interface IdBuilder {

    Long build(String nameSpace);

    default Long build() {
        return build("default");
    }

}
