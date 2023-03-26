package com.stellariver.milky.demo.basic;

import com.fasterxml.jackson.databind.JsonNode;
import com.stellariver.milky.common.base.Employee;
import com.stellariver.milky.common.tool.util.Json;
import com.stellariver.milky.common.tool.util.StreamMap;
import com.stellariver.milky.domain.support.base.ListTyped;
import com.stellariver.milky.domain.support.base.MapTyped;
import com.stellariver.milky.domain.support.base.Typed;
import com.stellariver.milky.domain.support.base.SetTyped;
import lombok.NonNull;

import java.util.*;

/**
 * @author houchuang
 */
@SuppressWarnings("unchecked")
public class TypedEnums {
    static public class CODES extends ListTyped<String> {}
    static public class EMPLOYEE extends Typed<Employee> {}
    static public class NUMBERS extends SetTyped<Integer> {}
    static public class MAP_NAME_TYPE extends MapTyped<Long, Long> {}
    static public class MARK_BEFORE extends Typed<Long> {}
    static public class MARK_HANDLE extends Typed<Long> {}
    static public class MARK_AFTER extends Typed<Long> {}

    static public String serialize(Map<Class<? extends Typed<?>>, Object> typedMap) {
        StreamMap<String, Object> init = StreamMap.init();
        typedMap.forEach((k, v) -> init.put(k.getSimpleName(), v));
        return Json.toJson(init.getMap());
    }

    static public Map<Class<? extends Typed<?>>, Object> deSerialize(@NonNull String value) {
        JsonNode jsonNode = Json.parseJsonNode(value);
        Map<Class<? extends Typed<?>>, Object> map = new HashMap<>(16);
        Arrays.stream(TypedEnums.class.getClasses()).filter(Typed.class::isAssignableFrom).forEach(typeEnum -> {
            Class<? extends Typed<?>> clazz = (Class<? extends Typed<?>>) typeEnum;
            JsonNode typeJsonNode = jsonNode.get(typeEnum.getSimpleName());
            if (typeJsonNode != null) {
                Typed<?> typed = Typed.transfer(clazz);
                Object o = typed.parseJsonNode(typeJsonNode);
                map.put(clazz, o);
            }
        });
        return map;
    }

}
