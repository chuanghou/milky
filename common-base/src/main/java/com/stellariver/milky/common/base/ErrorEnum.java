package com.stellariver.milky.common.base;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.apache.commons.text.StringSubstitutor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author houchuang
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ErrorEnum implements Serializable {

    String code;
    String message;

    public ErrorEnum(@NonNull String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ErrorEnum message(Supplier<String> supplier) {
        return message(supplier.get());
    }

    public ErrorEnum message(Object object) {
        return new ErrorEnum(code, object == null ? "NULL" : object.toString());
    }

    public ErrorEnum params(Map<String, Object> params) {
        return new ErrorEnum(code, StringSubstitutor.replace(message, params));
    }

    public ErrorEnum param(String name, Object object) {
        Map<String, Object> params = new HashMap<>();
        params.put(name, object);
        return new ErrorEnum(code, StringSubstitutor.replace(message, params));
    }

    @Override
    public String toString() {
        return String.format("code: %s, message: %s", code, message);
    }

}
