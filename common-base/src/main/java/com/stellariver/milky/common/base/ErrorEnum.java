package com.stellariver.milky.common.base;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * @author houchuang
 */
@Data
@NoArgsConstructor
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

    @Override
    public String toString() {
        return String.format("code: %s, message: %s", code, message);
    }

}
