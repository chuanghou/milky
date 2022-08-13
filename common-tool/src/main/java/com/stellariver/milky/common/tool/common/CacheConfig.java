package com.stellariver.milky.common.tool.common;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.concurrent.TimeUnit;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CacheConfig {

    boolean enable;

    Long maximumSize;

    Long expireAfterWrite;

    TimeUnit timeUnit;

}
