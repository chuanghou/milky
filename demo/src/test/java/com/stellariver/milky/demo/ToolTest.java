package com.stellariver.milky.demo;


import com.stellariver.milky.common.tool.util.Collect;

import java.util.Arrays;
import java.util.List;

public class ToolTest {

    public static void main(String[] args) {
        List<Integer> integers = Arrays.asList(1, 2, 5, 6);
        List<List<Integer>> collect = Collect.collect(integers, Collect.select(i -> i == 1, i -> i == 2));
        System.out.println(collect);
    }

}