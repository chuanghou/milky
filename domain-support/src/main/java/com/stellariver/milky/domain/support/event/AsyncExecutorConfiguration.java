package com.stellariver.milky.domain.support.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.concurrent.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AsyncExecutorConfiguration {

    int corePoolSize;

    int maximumPoolSize;

    int keepAliveTimeMinutes;

    int blockingQueueCapacity;

}
