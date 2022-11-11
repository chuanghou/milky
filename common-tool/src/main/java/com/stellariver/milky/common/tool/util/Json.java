package com.stellariver.milky.common.tool.util;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class Json {

    private static final JsonMapper MAPPER = JsonMapper.builder()
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(MapperFeature.AUTO_DETECT_FIELDS, true)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,false)
            .build();

    static {
        MAPPER.registerModule(new JavaTimeModule());
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
    public static JsonNode parseJsonNode(String json) {
        return MAPPER.readTree(json);
    }


    public static JsonNode toJsonNode(Object object) {
        return MAPPER.valueToTree(object);
    }

    @Nullable
    @SneakyThrows
    public static <T> T parse(JsonNode jsonNode, Class<T> clazz) {
        if (jsonNode == null) {
            return null;
        }
        return MAPPER.treeToValue(jsonNode, clazz);
    }

    @Nullable
    @SneakyThrows
    public static <T> List<T> parseList(JsonNode jsonNode, Class<T> clazz) {
        if (jsonNode == null) {
            return null;
        }
        JavaType type = MAPPER.getTypeFactory().constructParametricType(List.class, clazz);
        return MAPPER.treeToValue(jsonNode, type);
    }

    @Nullable
    @SneakyThrows
    public static <T> Set<T> parseSet(JsonNode jsonNode, Class<T> clazz) {
        if (jsonNode == null) {
            return null;
        }
        JavaType type = MAPPER.getTypeFactory().constructParametricType(Set.class, clazz);
        return MAPPER.treeToValue(jsonNode, type);
    }

    @Nullable
    @SneakyThrows
    public static <K, V> Map<K, V> parseMap(JsonNode jsonNode, Class<K> keyClazz, Class<V> valueClazz) {
        if (jsonNode == null) {
            return null;
        }
        return MAPPER.treeToValue(jsonNode, TypeFactory.defaultInstance().constructMapType(Map.class, keyClazz, valueClazz));
    }


}