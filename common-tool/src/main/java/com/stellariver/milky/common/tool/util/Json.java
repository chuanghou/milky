package com.stellariver.milky.common.tool.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.stellariver.milky.common.base.Result;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author houchuang
 */
@CustomLog
public class Json {

    private static final JsonMapper MAPPER = JsonMapper.builder()
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(MapperFeature.AUTO_DETECT_FIELDS, true)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,false)
            .configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
            .build();



    static {
        MAPPER.registerModule(new JavaTimeModule());
    }

    @SneakyThrows(JsonProcessingException.class)
    public static String toJson(@NonNull Object target) {
        return MAPPER.writeValueAsString(target);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <T> T parse(@NonNull String json, @NonNull Class<T> clazz) {
        return MAPPER.readValue(json, clazz);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <T> List<T> parseList(@NonNull String json, @NonNull Class<T> clazz) {
        JavaType type = MAPPER.getTypeFactory().constructParametricType(List.class, clazz);
        return MAPPER.readValue(json,type);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <T> Set<T> parseSet(@NonNull String json, @NonNull Class<T> clazz) {

        JavaType type = MAPPER.getTypeFactory().constructParametricType(Set.class, clazz);
        return MAPPER.readValue(json,type);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <K, V> Map<K, V> parseMap(@NonNull String json, @NonNull Class<K> keyClazz, @NonNull Class<V> valueClazz) {
        return MAPPER.readValue(json, TypeFactory.defaultInstance().constructMapType(Map.class, keyClazz, valueClazz));
    }

    @SneakyThrows(JsonProcessingException.class)
    public static JsonNode parseJsonNode(@NonNull String json) {
        return MAPPER.readTree(json);
    }


    public static JsonNode toJsonNode(@NonNull Object object) {
        return MAPPER.valueToTree(object);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <T> T parse(@NonNull JsonNode jsonNode, @NonNull Class<T> clazz) {
        return MAPPER.treeToValue(jsonNode, clazz);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <T> List<T> parseList(@NonNull JsonNode jsonNode, @NonNull Class<T> clazz) {
        JavaType type = MAPPER.getTypeFactory().constructParametricType(List.class, clazz);
        return MAPPER.treeToValue(jsonNode, type);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <T> Set<T> parseSet(@NonNull JsonNode jsonNode, @NonNull Class<T> clazz) {
        JavaType type = MAPPER.getTypeFactory().constructParametricType(Set.class, clazz);
        return MAPPER.treeToValue(jsonNode, type);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <K, V> Map<K, V> parseMap(@NonNull JsonNode jsonNode, @NonNull Class<K> keyClazz, @NonNull Class<V> valueClazz) {
        return MAPPER.treeToValue(jsonNode, TypeFactory.defaultInstance().constructMapType(Map.class, keyClazz, valueClazz));
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <T> Result<T> parseResult(@NonNull String json, @NonNull Class<T> clazz) {
        JavaType type = MAPPER.getTypeFactory().constructParametricType(Result.class, clazz);
        return MAPPER.readValue(json,type);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <T> Result<T> parseResult(@NonNull JsonNode jsonNode, @NonNull Class<T> clazz) {
        JavaType type = MAPPER.getTypeFactory().constructParametricType(Result.class, clazz);
        return MAPPER.treeToValue(jsonNode, type);
    }

}
