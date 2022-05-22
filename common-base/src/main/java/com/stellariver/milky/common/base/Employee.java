package com.stellariver.milky.common.base;

public class Employee implements Displayable{

    private final String id;

    private final String name;

    public static final Employee system = new Employee("system", "system");

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

    @Override
    public String display() {
        return "操作人: " + id + "_" + name;
    }
}
