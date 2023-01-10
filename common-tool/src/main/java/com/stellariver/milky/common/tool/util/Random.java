package com.stellariver.milky.common.tool.util;


/**
 * @author houchuang
 */
public class Random {

    static public long randomRange(long[] range) {
        return (long) (range[0] + (range[1] - range[0]) * Math.random());
    }
}
