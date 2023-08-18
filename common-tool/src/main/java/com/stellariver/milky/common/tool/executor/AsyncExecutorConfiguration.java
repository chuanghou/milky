package com.stellariver.milky.common.tool.executor;

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
public class AsyncExecutorConfiguration {

    int corePoolSize;

    int maximumPoolSize;

    int keepAliveTimeMinutes;

    int blockingQueueCapacity;

}
