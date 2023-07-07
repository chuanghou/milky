package com.stellariver.milky.demo;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.common.tool.util.Json;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CollectTest {


    @Test
    public void setTest() {
        Set<Integer> set1 = new HashSet<>(Arrays.asList(1, 2, 4, 5));
        Set<Integer> set2 = new HashSet<>(Arrays.asList(0, 3, 4, 6));
        Set<Integer> set3 = new HashSet<>(Collections.singletonList(4));
        Set<Integer> set4 = new HashSet<>(Arrays.asList(1, 2, 5));
        Set<Integer> set5 = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6));

        Set<Integer> inter = Collect.inter(set1, set2);
        Assertions.assertEquals(set3, inter);

        Set<Integer> subtract = Collect.subtract(set1, set2);
        Assertions.assertEquals(set4, subtract);

        Set<Integer> union = Collect.union(set1, set2);
        Assertions.assertEquals(set5, union);


    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    static class Student {

        @JsonSetter(nulls = Nulls.SKIP)
        String name = "Jack";
        Integer age;

    }

    @Test
    public void test() {
        Student build = Student.builder().age(11).build();
        String s = Json.toJson(build);
        Student parse = Json.parse(s, Student.class);
        System.out.println(parse.toString());
    }


    @Test
    public void testLevenshteinDistance() {
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
        System.out.println(levenshteinDistance.apply("中国", "他的测试内码"));
        System.out.println(levenshteinDistance.apply( "他的测试内码", "内码"));
        System.out.println(levenshteinDistance.apply("中国", "中国人"));
        System.out.println(levenshteinDistance.apply("你好", "我很好"));
        System.out.println(levenshteinDistance.apply("abcdef", "def"));
        System.out.println(levenshteinDistance.apply("测试内码", "他的测试内码"));

    }
}
