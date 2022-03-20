package com.stellariver.milky.common.base;

import lombok.ToString;

import java.util.Map;
import java.util.Optional;

@ToString
public class Code {

    private final String code;

    private final String message;

    private Map<String, Object> extendInfo;

    protected Code(String code, String message){
        this.code = code;
        this.message = message;
    }

    private Code(String code, String message, Map<String, Object> extendInfo) {
        this.code = code;
        this.message = message;
        this.extendInfo = extendInfo;
    }

    public static CodeBuilder builder() {
        return new CodeBuilder();
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

    static public Code code(String code) {
        return new Code(code, "undefined");
    }

    public Code message(String message) {
        return this.toBuilder().message(message).build();
    }

    public CodeBuilder toBuilder() {
        return new CodeBuilder().code(this.code).message(this.message).extendInfo(this.extendInfo);
    }

    public static class CodeBuilder {
        private String code;
        private String message;
        private Map<String, Object> extendInfo;

        CodeBuilder() {}

        public CodeBuilder code(String code) {
            this.code = code;
            return this;
        }

        public CodeBuilder message(String message) {
            this.message = message;
            return this;
        }

        public CodeBuilder extendInfo(Map<String, Object> extendInfo) {
            this.extendInfo = extendInfo;
            return this;
        }

        public Code build() {
            code = Optional.ofNullable(code).orElse("UNDEFINED");
            return new Code(code, message, extendInfo);
        }

        public String toString() {
            return "ErrorCode.ErrorCodeBuilder(code=" + this.code + ", message=" + this.message +  ", extendInfo=" + this.extendInfo + ")";
        }
    }
}
