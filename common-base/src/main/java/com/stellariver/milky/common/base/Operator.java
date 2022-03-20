package com.stellariver.milky.common.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
public class Operator {

    private String id;

    private String name;

    public static final Operator system = new Operator("system", "system");

}
