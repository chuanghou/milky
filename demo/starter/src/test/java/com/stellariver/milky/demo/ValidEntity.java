package com.stellariver.milky.demo;

import com.stellariver.milky.common.base.BizEx;
import com.stellariver.milky.common.base.AfterValidation;
import lombok.*;
import lombok.experimental.FieldDefaults;

import static com.stellariver.milky.common.base.ErrorEnumsBase.PARAM_IS_NULL;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ValidEntity {

    Long number;

    String name;


    @AfterValidation
    public void numberTest() {
        BizEx.trueThrow(number == null, PARAM_IS_NULL.message("number不能为空"), false);
    }

    @AfterValidation(groups = NameGroup.class)
    public void nameTest() {
        BizEx.trueThrow(name == null, PARAM_IS_NULL.message("name不能为空"), false);
    }

    interface NameGroup{}

}
