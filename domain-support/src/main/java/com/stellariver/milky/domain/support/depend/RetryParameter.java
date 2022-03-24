package com.stellariver.milky.domain.support.depend;

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

    int secondsToExpire;

    int times;

    long sleepTimeMils;

}
