package com.stellariver.milky.common.tool.validate;

import com.stellariver.milky.common.base.BizEx;
import com.stellariver.milky.common.base.CustomValid;
import com.stellariver.milky.common.base.ExceptionType;
import com.stellariver.milky.common.base.SysEx;
import com.stellariver.milky.common.tool.common.Kit;
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

import static com.stellariver.milky.common.base.ErrorEnumsBase.*;
import static com.stellariver.milky.common.tool.common.Kit.format;

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

    final static Map<Class<?>, Map<Class<?>, Method>> customValidMap = new ConcurrentHashMap<>();


    /**
     * Because JSR 303 doesn't support static method, this method omit constraints in method signature, but
     * the constraint inside java bean will be checked always
     */
    //TODO search on internet are there extend supports for static method ?
    public static void validate(Object object, Method method, Object returnValue, boolean failFast, ExceptionType type, Class<?>... groups) {
        if (!Modifier.isStatic(method.getModifiers())) {
            ExecutableValidator executableValidator = failFast ? EXECUTABLE_FAIL_FAST_VALIDATOR : EXECUTABLE_VALIDATOR;
            Set<ConstraintViolation<Object>> validateResult = executableValidator.validateReturnValue(object, method, returnValue, groups);
            check(validateResult, type);
        } else {
            logger.arg0(method.toGenericString()).warn("NOT_SUPPORT_STATIC_METHOD");
        }
        if (returnValue != null) {
            validate(returnValue, type, failFast, groups);
        }
    }

    /**
     * the same to above comment
     */
    public static void validate(Object object, Method method, Object[] params, boolean failFast, ExceptionType type, Class<?>... groups) {
        if (!Modifier.isStatic(method.getModifiers())) {
            ExecutableValidator executableValidator = failFast ? EXECUTABLE_FAIL_FAST_VALIDATOR : EXECUTABLE_VALIDATOR;
            Set<ConstraintViolation<Object>> validateResult = executableValidator.validateParameters(object, method, params, groups);
            check(validateResult, type);
        } else {
            logger.arg0(method.toGenericString()).warn("NOT_SUPPORT_STATIC_METHOD");
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
