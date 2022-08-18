package com.stellariver.milky.demo.basic;

import com.stellariver.milky.common.base.Employee;
import com.stellariver.milky.common.tool.common.Option;
import com.stellariver.milky.common.tool.common.SysException;
import com.stellariver.milky.common.tool.util.Json;
import com.stellariver.milky.common.tool.util.StreamMap;
import com.stellariver.milky.demo.domain.item.dependency.UserInfo;
import com.stellariver.milky.domain.support.base.NameType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NameTypes {

    static public final NameType<UserInfo> userInfo = new NameType<>("userInfo", UserInfo.class);

    static public final NameType<Employee> employee = new NameType<>("employee", Employee.class);

    static Map<String, NameType<?>> nameTypeMap = new HashMap<>();

    static {
        Arrays.stream(NameTypes.class.getDeclaredFields())
                .filter(field -> NameType.class.isAssignableFrom(field.getType()))
                .forEach(field -> {
                    NameType<?> fieldValue;
                    try {
                        fieldValue = (NameType<?>) field.get(null);
                    } catch (IllegalAccessException e) {
                        throw new SysException(e);
                    }
                    nameTypeMap.put(fieldValue.getName(), fieldValue);
                });
    }

    static public String serialize(Map<NameType<?>, Object> nameTypeMap) {
        nameTypeMap = Optional.ofNullable(nameTypeMap).orElseGet(HashMap::new);
        StreamMap<String, Object> init = StreamMap.init();
        nameTypeMap.forEach((k, v) -> init.put(k.getName(), v));
        return Json.toJson(init.getMap());
    }

    static public Map<NameType<?>, Object> deserialize(String nameTypeStringMap) {
        Map<String, String> stringMap = Json.parseMap(nameTypeStringMap, String.class, String.class);
        Map<NameType<?>, Object> map = new HashMap<>();
        stringMap.forEach((k, v) -> {
            NameType<?> nameType = nameTypeMap.get(k);
            map.put(nameType, nameType.parseJson(v));
        });
        return map;
    }
}
