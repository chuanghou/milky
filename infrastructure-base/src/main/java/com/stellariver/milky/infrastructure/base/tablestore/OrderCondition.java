package com.stellariver.milky.infrastructure.base.tablestore;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderCondition {

    String field;

    Order order;

}
