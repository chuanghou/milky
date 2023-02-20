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

import static com.stellariver.milky.demo.basic.TypedEnums.*;

@SuppressWarnings("unchecked")
public class TypedTest {

    @Test
    public void testNameType() {

        HashMap<Class<? extends Typed<?>>, Object> typedMap = new HashMap<>();
        Employee employee = new Employee();
        typedMap.put(EMPLOYEE.class, employee);
        Employee employee1 = (Employee) typedMap.get(EMPLOYEE.class);
        Assertions.assertEquals(employee, employee1);

        Set<Integer> integers = Collect.asSet(1, 2, 4);
        typedMap.put(NUMBERS.class, integers);

        List<String> strings = Collect.asList("121", "213", "231");
        typedMap.put(CODES.class, strings);

        Map<Long, Long> map = Collect.asMap(100L, 200L, 400L, 600L);
        typedMap.put(MAP_NAME_TYPE.class, map);

        String serialize = TypedEnums.serialize(typedMap);

        Map<Class<? extends Typed<?>>, Object> typedObjectMap = TypedEnums.deSerialize(serialize);

        Employee employee2 = (Employee) typedObjectMap.get(EMPLOYEE.class);
        Assertions.assertEquals(employee, employee2);

        List<String> strings1 = (List<String>) typedObjectMap.get(CODES.class);
        Assertions.assertEquals(strings, strings1);

        Set<Integer> integers1 = (Set<Integer>) typedObjectMap.get(NUMBERS.class);
        Assertions.assertEquals(integers1, integers);

        Map<Long, Long> longLongMap = (Map<Long, Long>) typedObjectMap.get(MAP_NAME_TYPE.class);
        Assertions.assertEquals(longLongMap, map);
    }
}
