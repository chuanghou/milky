package com.echobaba.milky.common.tool.util;


public class RandomUtils {

    static public long randomRange(long start, long end) {
        return (long) (start + (end - start) * Math.random());
    }
}
