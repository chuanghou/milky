package com.stellariver.milky.common.base;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.apache.commons.text.StringSubstitutor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author houchuang
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ErrorEnum {

    String code;
    String message;
    Map<String, Object> params;

    public ErrorEnum(@NonNull String code, String message, Map<String, Object> params) {
        this.code = code;
        this.message = message;
        this.params = params;
    }


    public ErrorEnum message(Supplier<String> supplier) {
        return message(supplier.get());
    }

    public ErrorEnum message(Object object) {
        return new ErrorEnum(code, StringSubstitutor.replace(object, params), params);
    }

    public ErrorEnum params(Map<String, Object> params) {
        return new ErrorEnum(code, StringSubstitutor.replace(message, params), params);
    }

    public ErrorEnum params(String name, Object object) {
        Map<String, Object> params = new HashMap<>();
        params.put(name, object);
        return new ErrorEnum(code, StringSubstitutor.replace(message, params), params);
    }

    @Override
    public String toString() {
        return String.format("code: %s, message: %s", code, message);
    }

}
