package com.stellariver.milky.demo;

import com.stellariver.milky.common.base.Employee;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.demo.basic.TypedEnums;
import com.stellariver.milky.domain.support.base.Typed;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TypedTest {

    @Test
    public void testNameType() {

        HashMap<Typed<?>, Object> typedMap = new HashMap<>();
        Employee employee = new Employee();
        typedMap.put(TypedEnums.employee, employee);
        Employee employee1 = TypedEnums.employee.extractFrom(typedMap);
        Assertions.assertEquals(employee, employee1);

        Set<Integer> integers = Collect.asSet(1, 2, 4);
        typedMap.put(TypedEnums.numbers, integers);

        List<String> strings = Collect.asList("121", "213", "231");
        typedMap.put(TypedEnums.codes, strings);

        Map<Long, Long> map = Collect.asMap(100L, 200L, 400L, 600L);
        typedMap.put(TypedEnums.mapNameType, map);

        String serialize = TypedEnums.serialize(typedMap);

        Map<Typed<?>, Object> typedObjectMap = TypedEnums.deSerialize(serialize);

        Employee employee2 = TypedEnums.employee.extractFrom(typedObjectMap);
        Assertions.assertEquals(employee, employee2);

        List<String> strings1 = TypedEnums.codes.extractFrom(typedObjectMap);
        Assertions.assertEquals(strings, strings1);

        Set<Integer> integers1 = TypedEnums.numbers.extractFrom(typedObjectMap);
        Assertions.assertEquals(integers1, integers);

        Map<Long, Long> longLongMap = TypedEnums.mapNameType.extractFrom(typedObjectMap);
        Assertions.assertEquals(longLongMap, map);
    }
}
