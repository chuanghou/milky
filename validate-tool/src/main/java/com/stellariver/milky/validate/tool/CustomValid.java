package com.stellariver.milky.validate.tool;

import javax.validation.groups.Default;
import java.lang.annotation.*;

/**
 * {@link ValidateUtil} could recognize a method with annotation {@link CustomValid} inside a param class
 * and invoke the method when the param instance is being checked its validity.
 * The method should be with void return type, public access and empty parameters.
 * if the param is not valid, a runtime exception should be thrown
 *
 *  <pre>
 * class Handler {
 *   &#64;Validate
 *   void handle(ParamToBeValidate param) {
 *     //business work
 *   }
 * }
 *
 * class ParamToBeValidate {
 *
 *   private Long number0;
 *   private Long number1
 *
 *   &#64;CustomValid
 *   public void customValid() {
 *     if (number0 == null || number1 == null) {
 *         throw new BizException("number 0 or number 1 is null")
 *     }
 *     if (number0 + number1 &gt; 100) {
 *         throw new BizException("number 0 plus number 1 is bigger than 100")
 *     }
 *   }
 *
 * }</pre>
 *
 *
 * @see ValidateUtil
 * @author hou chuang
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomValid {

    /**
     * JSR 303 group implementation
     */
    Class<?>[] groups() default Default.class;

}
