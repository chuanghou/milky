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
        NOT_WORK(-1),
        MONTH(1),
        WEEK(2),
        DAY(3);

        @Getter
        final Integer code;

        Duty(Integer code) {
            this.code = code;
        }
    }

}
