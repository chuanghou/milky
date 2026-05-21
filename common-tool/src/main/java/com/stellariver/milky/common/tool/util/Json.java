package com.stellariver.milky.common.tool.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.stellariver.milky.common.base.Result;
import lombok.CustomLog;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
            .build();

    static {
        MAPPER.registerModule(new JavaTimeModule());
    }

    @SneakyThrows(JsonProcessingException.class)
    public static String toJson(@NonNull Object target) {
        return MAPPER.writeValueAsString(target);
    }

    @SneakyThrows({ JsonProcessingException.class })
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
        return MAPPER.readValue(json, type);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <T> List<T> parseList(@NonNull String json, @NonNull Class<T> clazz, @NonNull JsonMapper jsonMapper) {
        JavaType type = jsonMapper.getTypeFactory().constructParametricType(List.class, clazz);
        return jsonMapper.readValue(json, type);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <T> Set<T> parseSet(@NonNull String json, @NonNull Class<T> clazz) {
        JavaType type = MAPPER.getTypeFactory().constructParametricType(Set.class, clazz);
        return MAPPER.readValue(json, type);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <T> Set<T> parseSet(@NonNull String json, @NonNull Class<T> clazz, @NonNull JsonMapper jsonMapper) {
        JavaType type = jsonMapper.getTypeFactory().constructParametricType(Set.class, clazz);
        return jsonMapper.readValue(json, type);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <K, V> Map<K, V> parseMap(@NonNull String json, @NonNull Class<K> keyClazz,
            @NonNull Class<V> valueClazz) {
        return MAPPER.readValue(json, TypeFactory.defaultInstance().constructMapType(Map.class, keyClazz, valueClazz));
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <K, V> Map<K, V> parseMap(@NonNull String json, @NonNull Class<K> keyClazz,
            @NonNull Class<V> valueClazz, @NonNull JsonMapper jsonMapper) {
        return jsonMapper.readValue(json,
                TypeFactory.defaultInstance().constructMapType(Map.class, keyClazz, valueClazz));
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
    public static <T> List<T> parseList(@NonNull JsonNode jsonNode, @NonNull Class<T> clazz,
            @NonNull JsonMapper jsonMapper) {
        JavaType type = jsonMapper.getTypeFactory().constructParametricType(List.class, clazz);
        return jsonMapper.treeToValue(jsonNode, type);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <T> Set<T> parseSet(@NonNull JsonNode jsonNode, @NonNull Class<T> clazz) {
        JavaType type = MAPPER.getTypeFactory().constructParametricType(Set.class, clazz);
        return MAPPER.treeToValue(jsonNode, type);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <T> Set<T> parseSet(@NonNull JsonNode jsonNode, @NonNull Class<T> clazz,
            @NonNull JsonMapper jsonMapper) {
        JavaType type = jsonMapper.getTypeFactory().constructParametricType(Set.class, clazz);
        return jsonMapper.treeToValue(jsonNode, type);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <K, V> Map<K, V> parseMap(@NonNull JsonNode jsonNode, @NonNull Class<K> keyClazz,
            @NonNull Class<V> valueClazz) {
        return MAPPER.treeToValue(jsonNode,
                TypeFactory.defaultInstance().constructMapType(Map.class, keyClazz, valueClazz));
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <K, V> Map<K, V> parseMap(@NonNull JsonNode jsonNode, @NonNull Class<K> keyClazz,
            @NonNull Class<V> valueClazz, @NonNull JsonMapper jsonMapper) {
        return jsonMapper.treeToValue(jsonNode,
                TypeFactory.defaultInstance().constructMapType(Map.class, keyClazz, valueClazz));
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <T> Result<T> parseResult(@NonNull String json, @NonNull Class<T> clazz) {
        JavaType type = MAPPER.getTypeFactory().constructParametricType(Result.class, clazz);
        return MAPPER.readValue(json, type);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <T> Result<T> parseResult(@NonNull String json, @NonNull Class<T> clazz,
            @NonNull JsonMapper jsonMapper) {
        JavaType type = jsonMapper.getTypeFactory().constructParametricType(Result.class, clazz);
        return jsonMapper.readValue(json, type);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <T> Result<T> parseResult(@NonNull JsonNode jsonNode, @NonNull Class<T> clazz) {
        JavaType type = MAPPER.getTypeFactory().constructParametricType(Result.class, clazz);
        return MAPPER.treeToValue(jsonNode, type);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <T> Result<T> parseResult(@NonNull JsonNode jsonNode, @NonNull Class<T> clazz,
            @NonNull JsonMapper jsonMapper) {
        JavaType type = jsonMapper.getTypeFactory().constructParametricType(Result.class, clazz);
        return jsonMapper.treeToValue(jsonNode, type);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <T> T parse(@NonNull String json, @NonNull TypeReference<T> typeReference) {
        return MAPPER.readValue(json, typeReference);
    }

    /**
     * 将类中的内部枚举类转换为丰富的 JSON 结构。
     * <p>
     * 遍历给定类的所有内部类，找出枚举类型，将每个枚举的所有成员转换为包含丰富信息的 JSON 对象。
     * </p>
     *
     * @param clazz 包含内部枚举类的类
     * @return JsonNode 结构：{ "枚举类简单名": [ { "name": "枚举名", "ordinal": 序号, "字段1": 值1,
     *         ... }, ... ], ... }
     */
    public static JsonNode enumToJsonNode(@NonNull Class<?> clazz) {
        ObjectNode result = MAPPER.createObjectNode();

        Class<?>[] declaredClasses = clazz.getDeclaredClasses();
        for (Class<?> innerClass : declaredClasses) {
            if (innerClass.isEnum()) {
                String enumName = innerClass.getSimpleName();
                ArrayNode enumArray = MAPPER.createArrayNode();

                Object[] enumConstants = innerClass.getEnumConstants();
                for (Object enumConstant : enumConstants) {
                    ObjectNode enumNode = MAPPER.createObjectNode();

                    // 添加枚举名和序号
                    enumNode.put("name", enumConstant.toString());
                    enumNode.put("ordinal", ((Enum<?>) enumConstant).ordinal());

                    // 遍历所有字段，添加字段值
                    Field[] fields = innerClass.getDeclaredFields();
                    for (Field field : fields) {
                        if (Modifier.isStatic(field.getModifiers())) {
                            continue; // 跳过静态字段（ENUM$VALUES 等）
                        }
                        try {
                            field.setAccessible(true);
                            Object value = field.get(enumConstant);
                            if (value != null) {
                                enumNode.set(field.getName(), MAPPER.valueToTree(value));
                            }
                        } catch (IllegalAccessException e) {
                            // 忽略访问异常
                        }
                    }

                    enumArray.add(enumNode);
                }

                result.set(enumName, enumArray);
            }
        }

        return result;
    }

}
