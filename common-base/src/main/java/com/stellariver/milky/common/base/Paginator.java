package com.stellariver.milky.common.base;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Paginator {

    long total;

    long pageNo;

    long pageSize;

    long pageCount;

}
