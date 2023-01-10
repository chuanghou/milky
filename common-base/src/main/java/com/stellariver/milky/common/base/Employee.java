package com.stellariver.milky.common.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author houchuang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee implements Displayable {

    private String id;

    private String name;

    public static final Employee SYSTEM = new Employee("system", "system");

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String getDisplay() {
        return id + "_" + name;
    }
}
