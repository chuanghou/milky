package com.stellariver.milky.common.tool;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.concurrent.TimeUnit;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CacheConfig {

    long maximumSize;

    long expireAfterWrite;

    TimeUnit timeUnit;

}
