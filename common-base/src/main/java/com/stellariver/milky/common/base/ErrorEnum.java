package com.stellariver.milky.common.base;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.apache.commons.text.StringSubstitutor;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ErrorEnum {

    String code;

    String message;

    Map<String, Object> extendInfo;

    ErrorEnum(@NonNull String code, String message, Map<String, Object> extendInfo) {
        this.code = code;
        this.message = message;
        this.extendInfo = extendInfo;
    }

    public String getCode() {
        return this.code;
    }

    public String getMessage() {
        return Optional.ofNullable(message).orElse(code);
    }

    static public ErrorEnum code(String code) {
        return new ErrorEnum(code, null, null);
    }

    public ErrorEnum message(Supplier<String> supplier) {
        this.message = StringSubstitutor.replace(supplier.get(), extendInfo);
        return this;
    }

    public ErrorEnum message(Object object) {
        this.message = StringSubstitutor.replace(object, extendInfo);
        return this;
    }

    public ErrorEnum extendInfo(Map<String, Object> extendInfo) {
        this.extendInfo = extendInfo;
        this.message = StringSubstitutor.replace(this.message, this.extendInfo);
        return this;
    }

    @Override
    public String toString() {
        return "ErrorEnum{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", extendInfo=" + extendInfo +
                '}';
    }
}
