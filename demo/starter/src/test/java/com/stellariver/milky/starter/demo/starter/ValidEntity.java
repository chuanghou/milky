package com.stellariver.milky.starter.demo.starter;

import com.stellariver.milky.common.tool.exception.BizEx;
import com.stellariver.milky.common.tool.validate.CustomValid;
import lombok.*;
import lombok.experimental.FieldDefaults;

import static com.stellariver.milky.common.tool.exception.ErrorEnumsBase.PARAM_IS_NULL;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ValidEntity {

    Long number;

    String name;


    @CustomValid
    public void numberTest() {
        BizEx.trueThrow(number == null, PARAM_IS_NULL.message("number不能为空"), false);

    }

    @CustomValid(groups = NameGroup.class)
    public void nameTest() {
        BizEx.trueThrow(name == null, PARAM_IS_NULL.message("name不能为空"), false);
    }

    interface NameGroup{}

}
