package com.stellariver.milky.validate.tool;

import com.stellariver.milky.common.base.ExceptionType;
import com.stellariver.milky.common.tool.exception.BizException;
import com.stellariver.milky.common.tool.exception.ErrorEnumsBase;
import com.stellariver.milky.common.tool.exception.SysException;
import com.stellariver.milky.common.tool.util.Collect;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.util.Strings;
import org.hibernate.validator.HibernateValidator;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;
import javax.validation.groups.Default;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

/**
 * @author houchuang
 */
public class ValidateUtil {

    static final private Validator FAIL_FAST_VALIDATOR = Validation.byProvider(HibernateValidator.class)
            .configure().failFast(true).buildValidatorFactory().getValidator();

    static final private ExecutableValidator EXECUTABLE_FAIL_FAST_VALIDATOR = FAIL_FAST_VALIDATOR.forExecutables();

    static final private Validator VALIDATOR = Validation.byProvider(HibernateValidator.class)
            .configure().buildValidatorFactory().getValidator();

    static final private ExecutableValidator EXECUTABLE_VALIDATOR = VALIDATOR.forExecutables();

    public static void validate(Object object, Method method, Object[] params,
                                boolean failFast, ExceptionType type, Class<?>... groups) throws BizException {
        validate(object, method, params, type, failFast, groups);
        Arrays.stream(params).filter(Objects::nonNull).forEach(param -> validate(param, type, failFast, groups));
    }

    public static void bizValidate(Object object, Method method, Object[] params,
                                   boolean failFast, Class<?>... groups) throws BizException {
        validate(object, method, params, ExceptionType.BIZ, failFast, groups);
        Arrays.stream(params).filter(Objects::nonNull).forEach(param -> validate(param, ExceptionType.BIZ, failFast, groups));
    }

    public static void sysValidate(Object object, Method method, Object[] params,
                                   boolean failFast, Class<?>... groups) throws SysException {
        validate(object, method, params, ExceptionType.SYS, failFast, groups);
        Arrays.stream(params).filter(Objects::nonNull).forEach(param -> validate(param, ExceptionType.SYS, failFast, groups));
    }

    public static void validate(Object object, Method method, Object[] params,
                                ExceptionType type, boolean failFast, Class<?>... groups) {
        ExecutableValidator executableValidator = failFast ? EXECUTABLE_FAIL_FAST_VALIDATOR : EXECUTABLE_VALIDATOR;
        Set<ConstraintViolation<Object>> validateResult = executableValidator.validateParameters(object, method, params, groups);
        check(validateResult, type);
        Arrays.stream(params).filter(Objects::nonNull).forEach(param -> validate(param, type, failFast, groups));
    }

    public static void validate(Object param) {
        validate(param, ExceptionType.BIZ, true);
    }

    final static Map<Pair<Class<?>, Class<?>>, Method> methodMap = new ConcurrentHashMap<>();

    final static Set<Class<?>> reflectedClasses = new CopyOnWriteArraySet<>();

    public static void validate(Object param, ExceptionType type, boolean failFast, Class<?>... groups) {
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
        if (!reflectedClasses.contains(clazz)) {
            List<Method> methods = Arrays.stream(param.getClass().getMethods())
                    .filter(m -> m.isAnnotationPresent(CustomValid.class)).collect(Collectors.toList());
            methods.forEach(m -> {
                Class<?> returnType = m.getReturnType();
                SysException.trueThrow(!returnType.equals(void.class), ErrorEnumsBase.CONFIG_ERROR.message("return type should void"));
                int modifiers = m.getModifiers();
                SysException.trueThrow(!Modifier.isPublic(modifiers), ErrorEnumsBase.CONFIG_ERROR.message("return type should public"));
            });
            methods.forEach(m -> {
                CustomValid anno = m.getAnnotation(CustomValid.class);
                for (Class<?> group : anno.groups()) {
                    Pair<Class<?>, Class<?>> classGroupKey = Pair.of(clazz, group);
                    SysException.trueThrow(methodMap.containsKey(classGroupKey),
                            ErrorEnumsBase.CONFIG_ERROR.message("same group should only has one custom valid method!"));
                    methodMap.put(classGroupKey, m);
                }
            });
            reflectedClasses.add(param.getClass());
        }

        List<Class<?>> groupList = groups.length == 0 ? Collect.asList(Default.class)
                : Arrays.stream(groups).collect(Collectors.toList());

        for (Class<?> g : groupList) {
            Method method = methodMap.get(Pair.of(clazz, g));
            if (method == null) {
                continue;
            }
            try {
                method.invoke(param);
            } catch (IllegalAccessException | InvocationTargetException e) {
                if (e instanceof InvocationTargetException && e.getCause() instanceof RuntimeException) {
                    throw ((RuntimeException) e.getCause());
                }
            }
        }
    }

    private static void check(Set<ConstraintViolation<Object>> validateResult, ExceptionType type) {
        if (Collect.isNotEmpty(validateResult)) {
            List<String> messages = Collect.transfer(validateResult, ConstraintViolation::getMessage);
            String message = Strings.join(messages, ';');
            if (type == ExceptionType.BIZ) {
                throw new BizException(ErrorEnumsBase.PARAM_FORMAT_WRONG.message(message));
            } else {
                throw new SysException(ErrorEnumsBase.PARAM_FORMAT_WRONG.message(message));
            }
        }
    }

}
