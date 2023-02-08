package com.stellariver.milky.domain.support.dependency;


import lombok.Getter;

/**
 * @author houchuang
 */
public interface IdBuilder {

    void initNameSpace(NameSpaceParam param);

    Long get(String nameSpace);

    void reset(String nameSpace);

    default Long get() {
        return get("default");
    }


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
