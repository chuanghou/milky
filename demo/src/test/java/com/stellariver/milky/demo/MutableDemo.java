package com.stellariver.milky.demo;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class MutableDemo {
    public static void main(String[] args) {

        Integer i = 100;
        Integer i1 = i;
        System.out.println(i == i1);
        i1 = 100;
        System.out.println(i == i1);
        i1 = IntStream.range(1, 2).map(p -> p * 100).reduce(0, Integer::sum);
        System.out.println(i == i1);


        Long s = 10000L;
        Long s1 = s;
        System.out.println(s == s1);
        s1 = 10000L;
        System.out.println(s == s1);
        s1 = LongStream.range(1L, 2L).map(p -> p * 10000L).reduce(0, Long::sum);
        System.out.println(s == s1);

        String m = "ss";
        String m1 = m;
        System.out.println(m == m1);
        m1 = "ss";
        System.out.println(m == m1);
        m1 = IntStream.range(0, 2).mapToObj(p -> "s").collect(Collectors.joining());
        System.out.println(m == m1);

    }
}
