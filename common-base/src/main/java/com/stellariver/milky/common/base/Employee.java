package com.stellariver.milky.common.base;

public class Employee {

    public static final Employee system = new Employee("system", "system");

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
        return this.name;
    }

}
