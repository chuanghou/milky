package com.stellariver.milky.demo.adapter.controller;

import com.stellariver.milky.common.base.BizEx;
import com.stellariver.milky.common.base.ErrorEnumsBase;
import com.stellariver.milky.common.base.Result;
import com.stellariver.milky.common.tool.JavaClassParser;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("tool")
public class ToolFacade {

    static private final Pattern scriptPattern = Pattern.compile("public\\s+class\\s+[a-zA-Z0-9_]+\\s+implements Callable<Object>\\s*\\{");
    static private final String SCRIPT_FORMAT = "";

    @SneakyThrows
    @PostMapping("executeScript")
    public Result<Object> executeScript(@RequestBody String script) {
        Matcher matcher = scriptPattern.matcher(script);
        String className;
        if (matcher.find()) {
            className = matcher.group().replace("{", " ").trim().split("\\s+")[2];
            if (matcher.find()) {
                throw new BizEx(ErrorEnumsBase.PARAM_FORMAT_WRONG.message("找到多个public入口类!"));
            }
        } else {
            throw new BizEx(ErrorEnumsBase.PARAM_FORMAT_WRONG.message("找不到public入口类!"));
        }

        Class<?> clazz = JavaClassParser.compile(className, script);
        Object o;
        try {
            Constructor<?> constructor = clazz.getConstructor();
            constructor.setAccessible(true);
            o = constructor.newInstance();
        } catch (NoSuchMethodException ex) {
            throw new BizEx(ErrorEnumsBase.PARAM_FORMAT_WRONG.message("应该提供默认构造函数"));
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
        Method method;
        try {
            method = clazz.getMethod("call");
        } catch (NoSuchMethodException ex) {
            throw new BizEx(ErrorEnumsBase.PARAM_FORMAT_WRONG.message("脚本类应该 import java.util.concurrent.Callable 接口!"));
        }
        Object data = method.invoke(o);
        return Result.success(data);

    }
}
