package com.stellariver.milky.financial.base;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Stock {

    String code;

    String name;

    String type;

    String exchange;

    BigDecimal closePrice;

}
