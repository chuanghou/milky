package com.echobaba.milky.client.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
public class Operator {

    private String operatorId;

    private String operatorName;

    public static final Operator system = new Operator("system", "system");

}
