package com.stellariver.milky.common.tool.util;


public class Random {

    static public long randomRange(long start, long end) {
        return (long) (start + (end - start) * Math.random());
    }
}
