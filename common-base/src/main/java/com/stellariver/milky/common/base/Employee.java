package com.stellariver.milky.common.base;

public class Employee {

    private final String id;

    private final String name;

    public Employee(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return name;
    }

    public static final Employee system = new Employee("system", "system");

}
