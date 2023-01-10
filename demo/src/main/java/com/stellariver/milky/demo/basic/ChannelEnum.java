package com.stellariver.milky.demo.basic;

import lombok.Getter;

/**
 * @author houchuang
 */

public enum ChannelEnum {

    ALI("阿里") {
        @Override
        public String test() {
            return null;
        }
    },
    JD("京东") {
        @Override
        public String test() {
            return null;
        }
    };

    @Getter
    private final String display;

    ChannelEnum(String display) {
        this.display = display;
    }

    abstract public String test();

}
