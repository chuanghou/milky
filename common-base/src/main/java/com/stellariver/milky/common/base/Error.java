package com.stellariver.milky.common.base;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class Error {

    private final String code;

    private final String message;

    private Map<String, Object> extendInfo;

    protected Error(String code, String message){
        this.code = code;
        this.message = message;
    }

    private Error(String code, String message, Map<String, Object> extendInfo) {
        this.code = code;
        this.message = message;
        this.extendInfo = extendInfo;
    }

    private static ErrorBuilder builder() {
        return new ErrorBuilder();
    }

    public String getCode() {
        return this.code;
    }

    public String getMessage() {
        return Optional.ofNullable(message).orElse(code);
    }

    public Map<String, Object> getExtendInfo() {
        return this.extendInfo;
    }

    static public Error code(String code) {
        return new Error(code, "undefined");
    }

    public Error message(Supplier<String> supplier) {
        return this.copy().message(supplier.get());
    }

    public Error message(String message) {
        return this.copy().message(message);
    }

    public Error message(Object object) {return this.copy().message(Objects.toString(object));}

    private Error copy() {
        return new ErrorBuilder().code(this.code).message(this.message).extendInfo(this.extendInfo).build();
    }

    @Override
    public String toString() {
        return "Error{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", extendInfo=" + extendInfo +
                '}';
    }

    private static class ErrorBuilder {
        private String code;
        private String message;
        private Map<String, Object> extendInfo;

        private ErrorBuilder() {}

        public ErrorBuilder code(String code) {
            this.code = code;
            return this;
        }

        public ErrorBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ErrorBuilder extendInfo(Map<String, Object> extendInfo) {
            this.extendInfo = extendInfo;
            return this;
        }

        public Error build() {
            code = Optional.ofNullable(code).orElse("UNDEFINED");
            return new Error(code, message, extendInfo);
        }

    }

}
