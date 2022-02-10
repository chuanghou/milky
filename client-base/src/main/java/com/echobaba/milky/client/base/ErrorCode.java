package com.echobaba.milky.client.base;

import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 这是一个有趣的人写的一个很有意思的错误码类，使用之前请阅读使用姿势手册
 * 成员变量解释：
 *  code： 一般情况下，我们会为错误码指定一个错误码，就是这里面的code变量，为了区别类名，这里特地定义为code，
 *  message：其实是用户可以看到的信息
 *  detailMessage：这里其实是打日志的内容，我坚持因为应该用异常包裹错误码，所以这里可以用于接受方打日志
 *  extendInfo：这里其实是一个附加信息，结构是以一个Pair的List结构，为什么不用Map，因为我认识这里的key可以重复
 *  messageParams：这里利用了一些SpringEl表达式的能力，可以通过模版化的message以及messageParams进行，变量替换，详细内容可以google spel
 *      如果你的项目不使用Spring，拜托你引用一下Spring expression的jar包，我不想扰乱你的pom依赖，所以依赖都是optional的
 *
 *  本类推荐的使用方式如下
 *  ErrorCode推荐使用建造者模式，在本应用中ErrorCode内部的code是唯一的，所以只能能过继承方式，定义枚举错误吗，可以更改相应ErrorCode内部的信息
 *  ，然后build，只是为了渲染Spring的El表达式
 *
 *  public class ErrorCodeEnum extends ErrorCode{
 *
 *     static public final ErrorCode repeatFolderName = ErrorCode.code("repeatFolderName").build();
 *
 * }
 */

@ToString
public class ErrorCode {

    private String code;

    private String message;

    private String detailMessage;

    // 扩展信息主要用来存储一些附加信息
    private List<Map<String, Object>> extendInfo;

    // 不允许任何不在枚举范围内的错误码，所有错误码需要基于错误码枚举生成, 所以禁止外部使用构造函数
    protected ErrorCode(){}

    private ErrorCode(String code,
                      String message,
                      String detailMessage,
                      List<Map<String, Object>> extendInfo) {
        this.code = code;
        this.message = message;
        this.detailMessage = detailMessage;
        this.extendInfo = extendInfo;
    }

    static private ThreadLocal<List<ErrorCode>>  temporaryErrorCodes;

    /**
     * 对于任何一个init函数都必须在有try-final块里面有 removeTemporaryErrorCodes（）操作
     */
    static public void initTemporaryErrorCodes() {
        ErrorCode.temporaryErrorCodes = new ThreadLocal<>();
        ErrorCode.temporaryErrorCodes.set(new ArrayList<>());
    }

    static public void  removeTemporaryErrorCodes() {
        Optional.ofNullable(temporaryErrorCodes).ifPresent(ThreadLocal::remove);
    }

    static public void  addTemporaryErrorCode(ErrorCode errorCode) {
        Optional.ofNullable(temporaryErrorCodes)
                .map(ThreadLocal::get)
                .orElseThrow(() -> new RuntimeException("temporaryErrorCodes 需要显式初始化"));
        temporaryErrorCodes.get().add(errorCode);
    }

    static public List<ErrorCode>  getTemporaryErrorCodes() {
        return Optional.ofNullable(temporaryErrorCodes).map(ThreadLocal::get).orElse(new ArrayList<>());
    }

    public String getCode() {
        return this.code;
    }

    public String getMessage() {
        return Optional.ofNullable(message).orElse(code);
    }

    public String getDetailMessage() {
        return this.detailMessage;
    }

    public List<Map<String, Object>> getExtendInfo() {
        return this.extendInfo;
    }

    static public ErrorCode code(String code) {
        ErrorCode errorCode = new ErrorCode();
        errorCode.code = code;
        return errorCode;
    }

    public ErrorCode message(String message) {
        this.message = message;
        return this;
    }

    public ErrorCode detailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
        return this;
    }

    public ErrorCode build() {
        return this;
    }

    static public ErrorCode withPrefix(Prefix prefix, String thirdCode) {
        return ErrorCode.code(prefix.getPreFix() + "_" + thirdCode);
    }
}
