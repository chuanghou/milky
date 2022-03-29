package com.stellariver.milky.domain.support.base;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RetryParameter {

    String lockKey;

    String encryptionKey;

    int milsToExpire;

    int times;

    long sleepTimeMils;

}
