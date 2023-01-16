package com.stellariver.milky.demo.basic;

import com.fasterxml.jackson.databind.JsonNode;
import com.stellariver.milky.common.base.Employee;
import com.stellariver.milky.common.tool.exception.BizException;
import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.exception.SysException;
import com.stellariver.milky.common.tool.util.Json;
import com.stellariver.milky.common.tool.util.StreamMap;
import com.stellariver.milky.demo.domain.item.dependency.UserInfo;
import com.stellariver.milky.domain.support.base.ListTyped;
import com.stellariver.milky.domain.support.base.MapTyped;
import com.stellariver.milky.domain.support.base.Typed;
import com.stellariver.milky.domain.support.base.SetTyped;
import lombok.NonNull;

import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * @author houchuang
 */
public class TypedEnums {

    static public Typed<UserInfo> userInfo;

    static public Typed<Employee> employee;

    static public ListTyped<String> codes;

    static public SetTyped<Integer> numbers;

    static public MapTyped<Long, Long> mapNameType;

    static Map<String, Typed<?>> typedMap = new HashMap<>();

    static {
        Arrays.stream(TypedEnums.class.getDeclaredFields())
                .filter(field -> Typed.class.isAssignableFrom(field.getType()))
                .forEach(field -> {
                    field.setAccessible(true);
                    Typed<?> value;
                    Class<?> type = field.getType();
                    if (type == Typed.class) {
                        Class<?> clazz = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                        value = new Typed<>();
                        value.setClazz(clazz);
                    } else if (type == ListTyped.class) {
                        Class<?> clazz = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                        ListTyped<?> tempValue = new ListTyped<>();
                        tempValue.setClazz(List.class);
                        tempValue.setVClazz(clazz);
                        value = tempValue;
                    } else if (type == SetTyped.class) {
                        Class<?> clazz = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                        SetTyped<?> tempValue = new SetTyped<>();
                        tempValue.setClazz(Set.class);
                        tempValue.setVClazz(clazz);
                        value = tempValue;
                    } else if (type == MapTyped.class) {
                        Class<?> kClazz = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                        Class<?> vClazz = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[1];
                        MapTyped<?, ?> tempValue = new MapTyped<>();
                        tempValue.setClazz(Map.class);
                        tempValue.setKClazz(kClazz);
                        tempValue.setVClazz(vClazz);
                        value = tempValue;
                    } else {
                        throw new SysException("unreached part!");
                    }
                    value.setName(field.getName());
                    try {
                        field.set(null, value);
                    } catch (IllegalAccessException e) {
                        throw new SysException(e);
                    }
                    boolean b = typedMap.containsKey(value.getName());
                    BizException.trueThrow(b, ErrorEnums.CONFIG_ERROR.message(String.format("同名%sNameType!", value.getName())));
                    typedMap.put(value.getName(), value);
                });
    }

    static public String serialize(Map<Typed<?>, Object> typedMap) {
        typedMap = Kit.op(typedMap).orElseGet(HashMap::new);
        StreamMap<String, Object> init = StreamMap.init();
        typedMap.forEach((k, v) -> init.put(k.getName(), v));
        return Json.toJson(init.getMap());
    }

    static public Map<Typed<?>, Object> deSerialize(@NonNull String value) {
        JsonNode jsonNode = Json.parseJsonNode(value);
        Map<Typed<?>, Object> map = new HashMap<>(16);
        typedMap.forEach((k, v) -> {
            JsonNode typeJsonNode = jsonNode.get(k);
            if (typeJsonNode != null) {
                Object o = v.parseJsonNode(typeJsonNode);
                map.put(v, o);
            }
        });
        return map;
    }

}
