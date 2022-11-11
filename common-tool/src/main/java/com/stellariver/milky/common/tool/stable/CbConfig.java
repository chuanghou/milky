package com.stellariver.milky.common.tool.stable;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Duration;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CbConfig {

    /**
     * 对应熔断器名称
     */
    String key;

    /**
     * 熔断器参数，以下参数均有默认值, 参看 CircuitBreakerConfig
     */
    private CircuitBreakerConfig.SlidingWindowType slidingWindowType;    // 熔断器关闭状态滑窗类型 默认 COUNT_BASED
    private Integer slidingWindowSize;                                  // 滑窗大小 默认100
    private Integer minimumNumberOfCalls;                               // 最小调用量，防止少量失败调用触发熔断 默认100
    private Float failureRateThreshold;                                 // 失败比例 默认50%

    private Float slowCallRateThreshold;                                // 慢调用比例 默认50%
    private Duration slowCallDurationThreshold;                         // 慢调用门槛值 默认5秒

    private Duration waitIntervalInOpenState;                           // 打开状态下，进入半打开状态下之前的等待时长, 默认5秒

    private Boolean automaticTransitionFromOpenToHalfOpenEnabled;       // 默认开启小流量探活能力

    private Integer permittedNumberOfCallsInHalfOpenState;              // 半开状态下小流量请求数量 半开状态探活请求数量

    /**
     * 熔断器操作
     */
    private Operation operation;                                       // 强制操作

    enum Operation {

        FORCE_OPEN,

        FORCE_CLOSE,

        RESET

    }
}
