package com.stellariver.milky.common.tool.common;

import com.stellariver.milky.common.tool.exception.BizException;
import com.stellariver.milky.common.tool.exception.ErrorEnumsBase;
import com.stellariver.milky.common.tool.exception.SysException;
import com.stellariver.milky.common.tool.util.Collect;
import org.hibernate.validator.HibernateValidator;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

/**
 * @author houchuang
 */
public class ValidateUtil {

    enum ExceptionType {

        // 业务异常
        BIZ,

        // 系统异常
        SYS

    }

    static final private Validator VALIDATOR = Validation.byProvider(HibernateValidator.class)
            .configure().failFast(true).buildValidatorFactory().getValidator();

    static final private ExecutableValidator EXECUTABLE_VALIDATOR = VALIDATOR.forExecutables();

    public static void bizValidate(Object object, Method method, Object[] params) throws BizException {
        validate(object, method, params, ExceptionType.BIZ);
        Arrays.stream(params).filter(Objects::nonNull).forEach(param -> validate(param, ExceptionType.BIZ));
    }

    public static void sysValidate(Object object, Method method, Object[] params) throws SysException {
        validate(object, method, params, ExceptionType.SYS);
        Arrays.stream(params).filter(Objects::nonNull).forEach(param -> validate(param, ExceptionType.SYS));
    }

    private static void validate(Object object, Method method, Object[] params, ExceptionType type) {
        Set<ConstraintViolation<Object>> validateResult = EXECUTABLE_VALIDATOR.validateParameters(object, method, params);
        check(validateResult, type);
        Arrays.stream(params).filter(Objects::nonNull).forEach(param -> validate(param, type));
    }

    private static void validate(Object param, ExceptionType type) {
        if (param instanceof Collection) {
            ((Collection<?>) param).forEach(p -> validate(p, type));
        }
        Set<ConstraintViolation<Object>> validateResult = VALIDATOR.validate(param);
        check(validateResult, type);
    }

    private static void check(Set<ConstraintViolation<Object>> validateResult, ExceptionType type) {
        if (Collect.isNotEmpty(validateResult)) {
            StringBuilder errorMsg = new StringBuilder();
            validateResult.forEach(validate -> errorMsg.append(validate.getPropertyPath()).append(validate.getMessage()).append(";"));
            if (type == ExceptionType.BIZ) {
                throw new BizException(ErrorEnumsBase.PARAM_FORMAT_WRONG.message(errorMsg.toString()));
            } else {
                throw new SysException(ErrorEnumsBase.PARAM_FORMAT_WRONG.message(errorMsg.toString()));
            }
        }
    }
}
