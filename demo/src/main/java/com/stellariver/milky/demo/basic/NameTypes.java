package com.stellariver.milky.demo.basic;

import com.stellariver.milky.common.base.Employee;
import com.stellariver.milky.common.tool.exception.BizException;
import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.exception.SysException;
import com.stellariver.milky.common.tool.util.Json;
import com.stellariver.milky.common.tool.util.StreamMap;
import com.stellariver.milky.demo.domain.item.dependency.UserInfo;
import com.stellariver.milky.domain.support.base.ListNameType;
import com.stellariver.milky.domain.support.base.MapNameType;
import com.stellariver.milky.domain.support.base.NameType;
import com.stellariver.milky.domain.support.base.SetNameType;
import lombok.NonNull;

import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * @author houchuang
 */
public class NameTypes {

    static public NameType<UserInfo> userInfo;

    static public NameType<Employee> employee;

    static public ListNameType<String> codes;

    static public SetNameType<Integer> numbers;

    static public MapNameType<Long, Long> mapNameType;

    static Map<String, NameType<?>> nameTypeMap = new HashMap<>();

    static {
        Arrays.stream(NameTypes.class.getDeclaredFields())
                .filter(field -> NameType.class.isAssignableFrom(field.getType()))
                .forEach(field -> {
                    field.setAccessible(true);
                    NameType<?> value;
                    Class<?> type = field.getType();
                    if (type == NameType.class) {
                        Class<?> clazz = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                        value = new NameType<>();
                        value.setClazz(clazz);
                    } else if (type == ListNameType.class) {
                        Class<?> clazz = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                        ListNameType<?> tempValue = new ListNameType<>();
                        tempValue.setClazz(List.class);
                        tempValue.setVClazz(clazz);
                        value = tempValue;
                    } else if (type == SetNameType.class) {
                        Class<?> clazz = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                        SetNameType<?> tempValue = new SetNameType<>();
                        tempValue.setClazz(Set.class);
                        tempValue.setClazz(clazz);
                        value = tempValue;
                    } else if (type == MapNameType.class) {
                        Class<?> kClazz = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                        Class<?> vClazz = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[1];
                        MapNameType<?, ?> tempValue = new MapNameType<>();
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
                    boolean b = nameTypeMap.containsKey(value.getName());
                    BizException.trueThrow(b, ErrorEnums.CONFIG_ERROR.message(String.format("同名%sNameType!", value.getName())));
                    nameTypeMap.put(value.getName(), value);
                });
    }

    static public String serialize(Map<NameType<?>, Object> nameTypeMap) {
        nameTypeMap = Kit.op(nameTypeMap).orElseGet(HashMap::new);
        StreamMap<String, Object> init = StreamMap.init();
        nameTypeMap.forEach((k, v) -> init.put(k.getName(), v));
        return Json.toJson(init.getMap());
    }

    static public Map<NameType<?>, Object> deSerialize(@NonNull String value) {
        Map<String, String> stringMap = Json.parseMap(value, String.class, String.class);
        Map<NameType<?>, Object> map = new HashMap<>(16);
        Kit.op(stringMap).orElseGet(HashMap::new).forEach((k, v) -> {
            NameType<?> nameType = nameTypeMap.get(k);
            map.put(nameType, nameType.parseJson(v));
        });
        return map;
    }

}
