package com.stellariver.milky.common.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author houchuang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee implements Serializable {

    @NotBlank(message = "id不可为空")
    private String id;

    @NotBlank(message = "姓名不可为空")
    private String name;

    public static final Employee SYSTEM = new Employee("system", "system");

}
