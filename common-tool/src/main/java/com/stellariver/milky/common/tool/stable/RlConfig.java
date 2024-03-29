package com.stellariver.milky.common.tool.stable;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Duration;

/**
 * @author houchuang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RlConfig {

    String ruleId;

    Double qps;

    Strategy strategy;

    Duration timeOut;

    Duration warningThreshold;

    public enum Strategy {

        FAIL_FAST,

        FAIL_TIME_OUT,

        FAIL_WAITING

    }
}
