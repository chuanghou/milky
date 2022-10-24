package com.stellariver.milky.spring.partner.limit;

import org.aspectj.lang.ProceedingJoinPoint;

public interface RateLimitConfigTunnel {

    Integer qps(String key);

    String key(ProceedingJoinPoint pjp);

}
