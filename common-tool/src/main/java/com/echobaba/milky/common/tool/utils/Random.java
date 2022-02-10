package com.echobaba.milky.common.tool.utils;


public class Random {

    static public long randomRange(long start, long end) {
        return (long) (start + (end - start) * Math.random());
    }
}
