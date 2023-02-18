package com.stellariver.milky.domain.support.dependency;


import lombok.Getter;

/**
 * @author houchuang
 */
public interface IdBuilder {
    long NULL_HOLDER_OF_LONG = -1L;

    void initNameSpace(Sequence param);

    Long get(String nameSpace);

    void reset(String nameSpace);

    enum Duty {
        NOT_WORK,
        MONTH,
        WEEK,
        DAY
    }

}
