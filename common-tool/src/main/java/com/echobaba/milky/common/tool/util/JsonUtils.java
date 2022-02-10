package com.echobaba.milky.common.tool.util;

import com.alibaba.c2m.milky.client.base.BizException;
import com.alibaba.c2m.milky.client.base.ErrorCode;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class JsonUtils {

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(MapperFeature.AUTO_DETECT_FIELDS, true)
            .build();

    public static String toString(Object target) {
        try {
            return MAPPER.writeValueAsString(target);
        } catch (Throwable throwable) {
            throw new BizException(ErrorCode.UNKNOWN, throwable);
        }
    }

    public static <T> T parse(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (Throwable throwable) {
            throw new BizException(ErrorCode.UNKNOWN, throwable);
        }
    }

    public static <T> List<T> parseList(String json, Class<T> clazz) {
        try {
            JavaType type = MAPPER.getTypeFactory().constructParametricType(List.class, clazz);
            return MAPPER.readValue(json,type);
        } catch (Throwable throwable) {
            throw new BizException(ErrorCode.UNKNOWN, throwable);
        }
    }

    public static <K, V> Map<K, V> parseMap(String json, Class<K> keyClazz, Class<V> valueClazz) {
        try {
            return MAPPER.readValue(json, TypeFactory.defaultInstance().constructMapType(Map.class, keyClazz, valueClazz));
        } catch (Throwable throwable) {
            throw new BizException(ErrorCode.UNKNOWN, throwable);
        }
    }

    public static JsonNode toJsonNode(String json) {
        try {
            return MAPPER.readTree(json);
        } catch (Throwable throwable) {
            throw new BizException(ErrorCode.UNKNOWN, throwable);
        }
    }
}