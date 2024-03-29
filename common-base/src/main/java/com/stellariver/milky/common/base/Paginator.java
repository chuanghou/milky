package com.stellariver.milky.common.base;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

/**
 * @author houchuang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Paginator implements Serializable {

    Integer total;

    Integer pageNo;

    Integer pageSize;

    Integer pageCount;

}
