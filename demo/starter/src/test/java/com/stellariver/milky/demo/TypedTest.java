package com.stellariver.milky.demo;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import com.stellariver.milky.common.base.Employee;
import com.stellariver.milky.common.tool.common.Typed;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.demo.basic.TypedEnums;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unchecked")
public class TypedTest {

    @Test
    public void testNameType() {

        HashMap<Class<? extends Typed<?>>, Object> typedMap = new HashMap<>();
        Employee employee = new Employee();
        typedMap.put(TypedEnums.EMPLOYEE.class, employee);
        Employee employee1 = (Employee) typedMap.get(TypedEnums.EMPLOYEE.class);
        Assertions.assertEquals(employee, employee1);

        Set<Integer> integers = Collect.asSet(1, 2, 4);
        typedMap.put(TypedEnums.NUMBERS.class, integers);

        List<String> strings = Collect.asList("121", "213", "231");
        typedMap.put(TypedEnums.CODES.class, strings);

        Map<Long, Long> map = Collect.asMap(100L, 200L, 400L, 600L);
        typedMap.put(TypedEnums.MAP_NAME_TYPE.class, map);

        String serialize = TypedEnums.serialize(typedMap);

        Map<Class<? extends Typed<?>>, Object> typedObjectMap = TypedEnums.deSerialize(serialize);

        Employee employee2 = (Employee) typedObjectMap.get(TypedEnums.EMPLOYEE.class);
        Assertions.assertEquals(employee, employee2);

        List<String> strings1 = (List<String>) typedObjectMap.get(TypedEnums.CODES.class);
        Assertions.assertEquals(strings, strings1);

        Set<Integer> integers1 = (Set<Integer>) typedObjectMap.get(TypedEnums.NUMBERS.class);
        Assertions.assertEquals(integers1, integers);

        Map<Long, Long> longLongMap = (Map<Long, Long>) typedObjectMap.get(TypedEnums.MAP_NAME_TYPE.class);
        Assertions.assertEquals(longLongMap, map);



    }

    @Test
    @SuppressWarnings({"beta", "UnstableApiUsage"})
    public void testToken() {
        RangeMap<Double, Pair<Double, Double>> rangeMap = TreeRangeMap.create();
        rangeMap.put(Range.closed(1D, 2D), Pair.of(1D, 2D));
        rangeMap.put(Range.singleton(1D), Pair.of(1D, 1D));
        System.out.println(rangeMap.get(1D));
        System.out.println(rangeMap.get(1.5D));

        Range<Double> closed = Range.closed(1D, 2D);
        Range<Double> closed1 = Range.closed(2D, 3D);
        Range<Double> intersection = closed1.intersection(closed);
        System.out.println(intersection);
        System.out.println(intersection.hasLowerBound());
        System.out.println(intersection.hasUpperBound());
        System.out.println(intersection.isEmpty());


    }
}
