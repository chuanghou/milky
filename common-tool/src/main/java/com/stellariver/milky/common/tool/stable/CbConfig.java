package com.stellariver.milky.common.tool.stable;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
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
public class CbConfig {

    /**
     * 对应熔断器名称
     */
    String key;

    /**
     * 熔断器参数，以下参数均有默认值, 参看 CircuitBreakerConfig
     * 熔断器关闭状态滑窗类型 默认 COUNT_BASED
      */
    private CircuitBreakerConfig.SlidingWindowType slidingWindowType;
    /**
     * 滑窗大小 默认100
     */
    private Integer slidingWindowSize;

    /**
     *   最小调用量，防止少量失败调用触发熔断 默认100
     */
    private Integer minimumNumberOfCalls;

    /**
     * 失败比例 默认50%
     */
    private Float failureRateThreshold;

    /**
     * 慢调用比例 默认50%
     */
    private Float slowCallRateThreshold;

    /**
     * 慢调用门槛值 默认5秒
     */
    private Duration slowCallDurationThreshold;

    /**
     * 打开状态下，进入半打开状态下之前的等待时长, 默认5秒
     */
    private Duration waitIntervalInOpenState;

    /**
     * 默认开启小流量探活能力
     */
    private Boolean automaticTransitionFromOpenToHalfOpenEnabled;

    /**
     * 半开状态下小流量请求数量 半开状态探活请求数量
     */
    private Integer permittedNumberOfCallsInHalfOpenState;

    /**
     * 熔断器操作
     */
    private Operation operation;

    enum Operation {

        FORCE_OPEN,

        FORCE_CLOSE,

        RESET

    }
}
