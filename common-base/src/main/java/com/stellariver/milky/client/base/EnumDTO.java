package com.stellariver.milky.client.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnumDTO {

    /**
     * 枚举code
     */
    private String code;

    /**
     * 枚举名称
     */
    private String name;
}
