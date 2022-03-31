package com.stellariver.milky.common.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
public class Employee {

    private String id;

    private String name;

    public static final Employee system = new Employee("system", "system");

}
