package com.stellariver.milky.common.tool.validate;

import com.stellariver.milky.common.base.ErrorEnumsBase;
import com.stellariver.milky.common.base.SysEx;
import com.stellariver.milky.common.base.Valids;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import javax.validation.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ValidsValidator implements ConstraintValidator<Valids, Object>  {

    static final private Validator VALIDATOR = Validation.byProvider(HibernateValidator.class)
            .configure().buildValidatorFactory().getValidator();

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {

        if (value == null) {
            return true;
        }

        if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            if (collection.size() == 0) {
                return true;
            }
            for (Object o : collection) {
                Set<ConstraintViolation<Object>> validateResult = VALIDATOR.validate(o);
                if (!validateResult.isEmpty()) {
                    HibernateConstraintValidatorContext hibernateContext
                            = constraintValidatorContext.unwrap(HibernateConstraintValidatorContext.class);
                    String internalMessage = validateResult.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(";"));
                    hibernateContext.addExpressionVariable("internalMessage", internalMessage);
                    return false;
                }
            }
        } else if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            if (map.size() == 0) {
                return true;
            }
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Set<ConstraintViolation<Object>> validateKeyResult = VALIDATOR.validate(entry.getKey());
                Set<ConstraintViolation<Object>> validateValueResult = VALIDATOR.validate(entry.getValue());
                List<ConstraintViolation<Object>> validateResult = Stream.of(validateKeyResult, validateValueResult).flatMap(Collection::stream).collect(Collectors.toList());
                if (!validateResult.isEmpty()) {
                    HibernateConstraintValidatorContext hibernateContext
                            = constraintValidatorContext.unwrap(HibernateConstraintValidatorContext.class);
                    String internalMessage = validateResult.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(";"));
                    hibernateContext.addExpressionVariable("internalMessage", internalMessage);
                    return false;
                }
            }
        } else {
            throw new SysEx(ErrorEnumsBase.VALIDATED_SUPPORT_COLLECTION_MAP);
        }
        return true;
    }

}
