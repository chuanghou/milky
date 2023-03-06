package com.stellariver.milky.financial.base;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Loan {

    Integer term;

    Long quantity;

    BigDecimal rate;

}
