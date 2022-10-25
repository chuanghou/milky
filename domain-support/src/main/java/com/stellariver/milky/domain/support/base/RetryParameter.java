package com.stellariver.milky.domain.support.base;

import com.stellariver.milky.common.tool.NameSpace;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RetryParameter {

    NameSpace nameSpace;

    String lockKey;

    String encryptionKey;

    int milsToExpire;

    int times;

    long sleepTimeMils;

}
