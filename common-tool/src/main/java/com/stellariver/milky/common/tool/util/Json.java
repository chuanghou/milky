package com.stellariver.milky.common.tool.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.stellariver.milky.common.base.Result;
import com.stellariver.milky.common.tool.common.Kit;
import lombok.CustomLog;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.util.*;

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

    @SneakyThrows({JsonProcessingException.class})
    public static String toJson(@NonNull Object target, @NonNull JsonMapper jsonMapper) {
        return jsonMapper.writeValueAsString(target);
    }


    @SneakyThrows(JsonProcessingException.class)
    public static <T> T parse(@NonNull String json, @NonNull Class<T> clazz) {
        return MAPPER.readValue(json, clazz);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <T> T parse(@NonNull String json, @NonNull Class<T> clazz, @NonNull JsonMapper jsonMapper) {
        return jsonMapper.readValue(json, clazz);
    }


    @SneakyThrows(JsonProcessingException.class)
    public static <T> List<T> parseList(@NonNull String json, @NonNull Class<T> clazz) {
        JavaType type = MAPPER.getTypeFactory().constructParametricType(List.class, clazz);
        return MAPPER.readValue(json,type);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <T> List<T> parseList(@NonNull String json, @NonNull Class<T> clazz, @NonNull JsonMapper jsonMapper) {
        JavaType type = jsonMapper.getTypeFactory().constructParametricType(List.class, clazz);
        return jsonMapper.readValue(json,type);
    }


    @SneakyThrows(JsonProcessingException.class)
    public static <T> Set<T> parseSet(@NonNull String json, @NonNull Class<T> clazz) {
        JavaType type = MAPPER.getTypeFactory().constructParametricType(Set.class, clazz);
        return MAPPER.readValue(json,type);
    }


    @SneakyThrows(JsonProcessingException.class)
    public static <T> Set<T> parseSet(@NonNull String json, @NonNull Class<T> clazz, @NonNull JsonMapper jsonMapper) {
        JavaType type = jsonMapper.getTypeFactory().constructParametricType(Set.class, clazz);
        return jsonMapper.readValue(json,type);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <K, V> Map<K, V> parseMap(@NonNull String json, @NonNull Class<K> keyClazz, @NonNull Class<V> valueClazz) {
        return MAPPER.readValue(json, TypeFactory.defaultInstance().constructMapType(Map.class, keyClazz, valueClazz));
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <K, V> Map<K, V> parseMap(@NonNull String json, @NonNull Class<K> keyClazz, @NonNull Class<V> valueClazz, @NonNull JsonMapper jsonMapper) {
        return jsonMapper.readValue(json, TypeFactory.defaultInstance().constructMapType(Map.class, keyClazz, valueClazz));
    }


    @SneakyThrows(JsonProcessingException.class)
    public static JsonNode parseJsonNode(@NonNull String json) {
        return MAPPER.readTree(json);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static JsonNode parseJsonNode(@NonNull String json, @NonNull JsonMapper jsonMapper) {
        return jsonMapper.readTree(json);
    }

    public static JsonNode toJsonNode(@NonNull Object object) {
        return MAPPER.valueToTree(object);
    }

    public static JsonNode toJsonNode(@NonNull Object object, @NonNull JsonMapper jsonMapper) {
        return jsonMapper.valueToTree(object);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <T> T parse(@NonNull JsonNode jsonNode, @NonNull Class<T> clazz) {
        return MAPPER.treeToValue(jsonNode, clazz);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <T> T parse(@NonNull JsonNode jsonNode, @NonNull Class<T> clazz, @NonNull JsonMapper jsonMapper) {
        return jsonMapper.treeToValue(jsonNode, clazz);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <T> List<T> parseList(@NonNull JsonNode jsonNode, @NonNull Class<T> clazz) {
        JavaType type = MAPPER.getTypeFactory().constructParametricType(List.class, clazz);
        return MAPPER.treeToValue(jsonNode, type);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <T> List<T> parseList(@NonNull JsonNode jsonNode, @NonNull Class<T> clazz, @NonNull JsonMapper jsonMapper) {
        JavaType type = jsonMapper.getTypeFactory().constructParametricType(List.class, clazz);
        return jsonMapper.treeToValue(jsonNode, type);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <T> Set<T> parseSet(@NonNull JsonNode jsonNode, @NonNull Class<T> clazz) {
        JavaType type = MAPPER.getTypeFactory().constructParametricType(Set.class, clazz);
        return MAPPER.treeToValue(jsonNode, type);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <T> Set<T> parseSet(@NonNull JsonNode jsonNode, @NonNull Class<T> clazz, @NonNull JsonMapper jsonMapper) {
        JavaType type = jsonMapper.getTypeFactory().constructParametricType(Set.class, clazz);
        return jsonMapper.treeToValue(jsonNode, type);
    }


    @SneakyThrows(JsonProcessingException.class)
    public static <K, V> Map<K, V> parseMap(@NonNull JsonNode jsonNode, @NonNull Class<K> keyClazz, @NonNull Class<V> valueClazz) {
        return MAPPER.treeToValue(jsonNode, TypeFactory.defaultInstance().constructMapType(Map.class, keyClazz, valueClazz));
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <K, V> Map<K, V> parseMap(@NonNull JsonNode jsonNode, @NonNull Class<K> keyClazz, @NonNull Class<V> valueClazz, @NonNull JsonMapper jsonMapper) {
        return jsonMapper.treeToValue(jsonNode, TypeFactory.defaultInstance().constructMapType(Map.class, keyClazz, valueClazz));
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <T> Result<T> parseResult(@NonNull String json, @NonNull Class<T> clazz) {
        JavaType type = MAPPER.getTypeFactory().constructParametricType(Result.class, clazz);
        return MAPPER.readValue(json,type);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <T> Result<T> parseResult(@NonNull String json, @NonNull Class<T> clazz, @NonNull JsonMapper jsonMapper) {
        JavaType type = jsonMapper.getTypeFactory().constructParametricType(Result.class, clazz);
        return jsonMapper.readValue(json,type);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <T> Result<T> parseResult(@NonNull JsonNode jsonNode, @NonNull Class<T> clazz) {
        JavaType type = MAPPER.getTypeFactory().constructParametricType(Result.class, clazz);
        return MAPPER.treeToValue(jsonNode, type);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <T> Result<T> parseResult(@NonNull JsonNode jsonNode, @NonNull Class<T> clazz, @NonNull JsonMapper jsonMapper) {
        JavaType type = jsonMapper.getTypeFactory().constructParametricType(Result.class, clazz);
        return jsonMapper.treeToValue(jsonNode, type);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <T> T parse(@NonNull String json, @NonNull TypeReference<T> typeReference) {
        return MAPPER.readValue(json, typeReference);
    }

}
