package com.stellariver.milky.common.tool.util;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class Json {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(MapperFeature.AUTO_DETECT_FIELDS,true);
    }

    @SneakyThrows
    public static String toJson(Object target) {
        return MAPPER.writeValueAsString(target);
    }

    @SneakyThrows
    public static String toJson(Object... objects) {
        return MAPPER.writeValueAsString(objects);
    }

    @Nullable
    @SneakyThrows
    public static <T> T parse(String json, Class<T> clazz) {
        if (json == null) {
            return null;
        }
        return MAPPER.readValue(json, clazz);
    }

    @Nullable
    @SneakyThrows
    public static <T> List<T> parseList(String json, Class<T> clazz) {
        if (json == null) {
            return null;
        }
        JavaType type = MAPPER.getTypeFactory().constructParametricType(List.class, clazz);
        return MAPPER.readValue(json,type);
    }

    @Nullable
    @SneakyThrows
    public static <T> Set<T> parseSet(String json, Class<T> clazz) {
        if (json == null) {
            return null;
        }
        JavaType type = MAPPER.getTypeFactory().constructParametricType(Set.class, clazz);
        return MAPPER.readValue(json,type);
    }

    @Nullable
    @SneakyThrows
    public static <K, V> Map<K, V> parseMap(String json, Class<K> keyClazz, Class<V> valueClazz) {
        if (json == null) {
            return null;
        }
        return MAPPER.readValue(json, TypeFactory.defaultInstance().constructMapType(Map.class, keyClazz, valueClazz));
    }

    @SneakyThrows
    public static JsonNode toJsonNode(String json) {
        return MAPPER.readTree(json);
    }

}