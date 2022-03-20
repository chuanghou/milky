package com.stellariver.milky.common.base;

import lombok.ToString;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ToString
public class ErrorCode {

    private String code;

    private String message;

    // 扩展信息主要用来存储一些附加信息
    private Map<String, Object> extendInfo;

    // 不允许任何不在枚举范围内的错误码，所有错误码需要基于错误码枚举生成, 所以禁止外部使用构造函数
    protected ErrorCode(){}

    private ErrorCode(String code,
                      String message,
                      Map<String, Object> extendInfo) {
        this.code = code;
        this.message = message;
        this.extendInfo = extendInfo;
    }

    public static ErrorCodeBuilder builder() {
        return new ErrorCodeBuilder();
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

    static public ErrorCode code(String code) {
        ErrorCode errorCode = new ErrorCode();
        errorCode.code = code;
        return errorCode;
    }

    public ErrorCode message(String message) {
        return this.toBuilder().message(message).build();
    }

    public ErrorCodeBuilder toBuilder() {
        return new ErrorCodeBuilder().code(this.code).message(this.message).extendInfo(this.extendInfo);
    }

    public static class ErrorCodeBuilder {
        private String code;
        private String message;
        private Map<String, Object> extendInfo;

        ErrorCodeBuilder() {
        }

        public ErrorCodeBuilder code(String code) {
            this.code = code;
            return this;
        }

        public ErrorCodeBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ErrorCodeBuilder extendInfo(Map<String, Object> extendInfo) {
            this.extendInfo = extendInfo;
            return this;
        }

        public ErrorCode build() {
            code = Optional.ofNullable(code).orElse("UNDEFINED");
            return new ErrorCode(code, message, extendInfo);
        }

        public String toString() {
            return "ErrorCode.ErrorCodeBuilder(code=" + this.code + ", message=" + this.message +  ", extendInfo=" + this.extendInfo + ")";
        }
    }
}
