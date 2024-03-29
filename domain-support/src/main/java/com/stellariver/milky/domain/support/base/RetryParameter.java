package com.stellariver.milky.domain.support.base;

import com.stellariver.milky.common.tool.common.UK;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * @author houchuang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RetryParameter {

    UK nameSpace;

    String lockKey;

    int milsToExpire;

    int times;

    long sleepTimeMils;

}
