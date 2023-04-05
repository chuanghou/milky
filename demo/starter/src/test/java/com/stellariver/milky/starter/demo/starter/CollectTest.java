package com.stellariver.milky.starter.demo.starter;

import com.stellariver.milky.common.tool.util.Collect;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
}
