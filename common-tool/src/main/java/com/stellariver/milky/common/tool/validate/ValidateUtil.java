package com.stellariver.milky.common.tool.validate;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.stellariver.milky.common.base.*;
import com.stellariver.milky.common.tool.log.Logger;
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

import static com.stellariver.milky.common.base.ErrorEnumsBase.CONFIG_ERROR;
import static com.stellariver.milky.common.base.ErrorEnumsBase.PARAM_FORMAT_WRONG;

/**
 * @author houchuang
 */
public class ValidateUtil {

    static private final Logger logger = Logger.getLogger(ValidateUtil.class);

    @SuppressWarnings("resource")
    static final private Validator FAIL_FAST_VALIDATOR = Validation.byProvider(HibernateValidator.class)
            .configure().failFast(true).buildValidatorFactory().getValidator();

    static final private ExecutableValidator EXECUTABLE_FAIL_FAST_VALIDATOR = FAIL_FAST_VALIDATOR.forExecutables();

    @SuppressWarnings("resource")
    static final private Validator VALIDATOR = Validation.byProvider(HibernateValidator.class)
            .configure().buildValidatorFactory().getValidator();

    static final private ExecutableValidator EXECUTABLE_VALIDATOR = VALIDATOR.forExecutables();

    final static Map<Class<?>, ListMultimap<Class<?>, Method>> afterValidationMap = new ConcurrentHashMap<>();
    final static Map<Class<?>, ListMultimap<Class<?>, Method>> beforeValidationMap = new ConcurrentHashMap<>();

    /**
     *
     * Because JSR 303 doesn't support static method, this method omit constraints in method signature, but
     * the constraint inside java bean will be checked always
     * @param object target bean
     * @param method target method
     * @param returnValue the return value
     * @param failFast true or false
     * @param type exception type
     * @param groups the selected group
     */
    public static void validate(Object object, Method method, Object returnValue, boolean failFast, ExceptionType type, Class<?>... groups) {
        if (!Modifier.isStatic(method.getModifiers())) {
            ExecutableValidator executableValidator = failFast ? EXECUTABLE_FAIL_FAST_VALIDATOR : EXECUTABLE_VALIDATOR;
            Set<ConstraintViolation<Object>> validateResult = executableValidator.validateReturnValue(object, method, returnValue, groups);
            check(validateResult, type);
        } else {
            throw new SysEx(ErrorEnumsBase.NOT_SUPPORT_STATIC_METHOD);
        }
        if (returnValue != null) {
            validate(returnValue, type, failFast, groups);
        }
    }

    /**
     *
     * Because JSR 303 doesn't support static method, this method omit constraints in method signature, but
     * the constraint inside java bean will be checked always
     * @param object target bean
     * @param method target method
     * @param failFast true or false
     * @param type exception type
     * @param groups the selected group
     */
    public static void validate(Object object, Method method, Object[] params, boolean failFast, ExceptionType type, Class<?>... groups) {
        if (!Modifier.isStatic(method.getModifiers())) {
            ExecutableValidator executableValidator = failFast ? EXECUTABLE_FAIL_FAST_VALIDATOR : EXECUTABLE_VALIDATOR;
            Set<ConstraintViolation<Object>> validateResult = executableValidator.validateParameters(object, method, params, groups);
            check(validateResult, type);
        } else {
            throw new SysEx(ErrorEnumsBase.NOT_SUPPORT_STATIC_METHOD);
        }
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

        Class<?> clazz = param.getClass();
        ListMultimap<Class<?>, Method> beforeValidations = beforeValidationMap.get(clazz);
        if (beforeValidations == null){
            List<Method> methods = Reflect.ancestorClasses(param.getClass()).stream().flatMap(c -> Arrays.stream(c.getDeclaredMethods()))
                    .filter(m -> m.isAnnotationPresent(BeforeValidation.class)).peek(CUSTOM_VALID_FORMAT).collect(Collectors.toList());

            beforeValidations = ArrayListMultimap.create();
            for (Method method : methods) {
                BeforeValidation anno = method.getAnnotation(BeforeValidation.class);
                List<Class<?>> groupList =  anno.groups().length == 0 ? Collect.asList(Default.class) : Collect.asList(anno.groups());
                for (Class<?> group : groupList) {
                    beforeValidations.put(group, method);
                }
            }
            beforeValidationMap.put(clazz, beforeValidations);
        }

        for (Class<?> g : groups.length == 0 ? Collect.asList(Default.class) : Collect.asList(groups)) {
            beforeValidations.get(g).forEach(m -> Reflect.invoke(m, param));
        }


        Validator validator = failFast ? FAIL_FAST_VALIDATOR : VALIDATOR;
        Set<ConstraintViolation<Object>> validateResult = validator.validate(param, groups);
        check(validateResult, type);


        ListMultimap<Class<?>, Method> afterValidations = afterValidationMap.get(clazz);
        if (afterValidations == null){
            List<Method> methods = Reflect.ancestorClasses(param.getClass()).stream().flatMap(c -> Arrays.stream(c.getDeclaredMethods()))
                    .filter(m -> m.isAnnotationPresent(AfterValidation.class)).peek(CUSTOM_VALID_FORMAT).collect(Collectors.toList());

            afterValidations = ArrayListMultimap.create();
            for (Method method : methods) {
                AfterValidation anno = method.getAnnotation(AfterValidation.class);
                List<Class<?>> groupList =  anno.groups().length == 0 ? Collect.asList(Default.class) : Collect.asList(anno.groups());
                for (Class<?> group : groupList) {
                    afterValidations.put(group, method);
                }
            }
            afterValidationMap.put(clazz, afterValidations);
        }

        for (Class<?> g : groups.length == 0 ? Collect.asList(Default.class) : Collect.asList(groups)) {
            afterValidations.get(g).forEach(m -> Reflect.invoke(m, param));
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
