package com.stellariver.milky.common.tool.validate;

import com.stellariver.milky.common.base.ExceptionType;
import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.exception.BizEx;
import com.stellariver.milky.common.tool.exception.SysEx;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.common.tool.util.Reflect;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.HibernateValidator;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;
import javax.validation.groups.Default;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.stellariver.milky.common.tool.common.Kit.format;
import static com.stellariver.milky.common.tool.exception.ErrorEnumsBase.*;

/**
 * @author houchuang
 */
public class ValidateUtil {

    @SuppressWarnings("resource")
    static final private Validator FAIL_FAST_VALIDATOR = Validation.byProvider(HibernateValidator.class)
            .configure().failFast(true).buildValidatorFactory().getValidator();

    static final private ExecutableValidator EXECUTABLE_FAIL_FAST_VALIDATOR = FAIL_FAST_VALIDATOR.forExecutables();

    @SuppressWarnings("resource")
    static final private Validator VALIDATOR = Validation.byProvider(HibernateValidator.class)
            .configure().buildValidatorFactory().getValidator();

    static final private ExecutableValidator EXECUTABLE_VALIDATOR = VALIDATOR.forExecutables();

    final static Map<Class<?>, Map<Class<?>, Method>> customValidMap = new ConcurrentHashMap<>();

    public static void validate(Object object, Method method, Object[] params,
                                boolean failFast, ExceptionType type, Class<?>... groups) {
        validate(object, method, params, type, failFast, groups);
        Arrays.stream(params).filter(Objects::nonNull).forEach(param -> validate(param, type, failFast, groups));
    }

    public static void validate(Object object, Method method, Object returnValue,
                                boolean failFast, ExceptionType type, Class<?>... groups) {
        ExecutableValidator executableValidator = failFast ? EXECUTABLE_FAIL_FAST_VALIDATOR : EXECUTABLE_VALIDATOR;
        Set<ConstraintViolation<Object>> validateResult = executableValidator.validateReturnValue(object, method, returnValue, groups);
        check(validateResult, type);
    }

    public static void bizValidate(Object object, Method method, Object[] params,
                                   boolean failFast, Class<?>... groups) throws BizEx {
        validate(object, method, params, ExceptionType.BIZ, failFast, groups);
        Arrays.stream(params).filter(Objects::nonNull).forEach(param -> validate(param, ExceptionType.BIZ, failFast, groups));
    }

    @SuppressWarnings("unused")
    public static void sysValidate(Object object, Method method, Object[] params,
                                   boolean failFast, Class<?>... groups) throws SysEx {
        validate(object, method, params, ExceptionType.SYS, failFast, groups);
        Arrays.stream(params).filter(Objects::nonNull).forEach(param -> validate(param, ExceptionType.SYS, failFast, groups));
    }

    public static void validate(Object object, Method method, Object[] params,
                                ExceptionType type, boolean failFast, Class<?>... groups) {
        ExecutableValidator executableValidator = failFast ? EXECUTABLE_FAIL_FAST_VALIDATOR : EXECUTABLE_VALIDATOR;
        if (!Modifier.isStatic(method.getModifiers())) {
            Set<ConstraintViolation<Object>> validateResult = executableValidator.validateParameters(object, method, params, groups);
            check(validateResult, type);
        }
        // static method not supported
        Arrays.stream(params).filter(Objects::nonNull).forEach(param -> validate(param, type, failFast, groups));
    }

    public static void validate(Object param) {
        validate(param, ExceptionType.BIZ, true);
    }


    static private final Consumer<Method> CUSTOM_VALID_FORMAT = m -> {
        boolean instanceMethod = !Modifier.isStatic(m.getModifiers());
        boolean zeroParams = m.getParameterTypes().length == 0;
        boolean voidReturn = m.getReturnType().equals(void.class);
        boolean pub = Modifier.isPublic(m.getModifiers());
        boolean b = instanceMethod && zeroParams && voidReturn && pub;
        SysEx.falseThrow(b, CONFIG_ERROR.message(m.toGenericString()));
    };

    public static void validate(Object param, ExceptionType type, boolean failFast, Class<?>... groups) {
        // when the param is collection or map, check its java bean
        if (param instanceof Collection) {
            ((Collection<?>) param).forEach(p -> validate(p, type, failFast, groups));
        } else if (param instanceof Map) {
            ((Map<?, ?>) param).forEach((k, v) -> {
                validate(k, type, failFast, groups);
                validate(v, type, failFast, groups);
            });
        }

        Validator validator = failFast ? FAIL_FAST_VALIDATOR : VALIDATOR;
        Set<ConstraintViolation<Object>> validateResult = validator.validate(param, groups);
        check(validateResult, type);


        Class<?> clazz = param.getClass();
        Map<Class<?>, Method> customValidations = customValidMap.get(clazz);
        if (customValidations == null){
            List<Method> methods = Arrays.stream(param.getClass().getDeclaredMethods())
                    .filter(m -> m.isAnnotationPresent(CustomValid.class)).peek(CUSTOM_VALID_FORMAT).collect(Collectors.toList());

            customValidations = new HashMap<>();
            for (Method method : methods) {
                CustomValid anno = method.getAnnotation(CustomValid.class);
                List<Class<?>> groupList =  anno.groups().length == 0 ? Collect.asList(Default.class) : Collect.asList(anno.groups());
                for (Class<?> group : groupList) {
                    Method oldValue = customValidations.put(group, method);
                    SysEx.trueThrow(oldValue != null,
                            REPEAT_VALIDATE_GROUP.message(format("repeat group %s validation", group)));
                }
            }
            customValidMap.put(clazz, customValidations);
        }

        List<Class<?>> groupList = groups.length == 0 ? Collect.asList(Default.class) : Collect.asList(groups);
        for (Class<?> g : groupList) {
            Kit.op(customValidMap.get(clazz)).map(map -> map.get(g)).ifPresent(m -> Reflect.invoke(m, param));
        }

    }

    private static void check(Set<ConstraintViolation<Object>> validateResult, ExceptionType type) {
        if (Collect.isNotEmpty(validateResult)) {
            List<String> messages = Collect.transfer(validateResult, ConstraintViolation::getMessage);
            String message = StringUtils.join(messages, ';');
            if (type == ExceptionType.BIZ) {
                throw new BizEx(PARAM_FORMAT_WRONG.message(message));
            } else {
                throw new SysEx(PARAM_FORMAT_WRONG.message(message));
            }
        }
    }

}
