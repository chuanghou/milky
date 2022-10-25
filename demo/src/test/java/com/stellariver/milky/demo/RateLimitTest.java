package com.stellariver.milky.demo;

import com.stellariver.milky.common.tool.exception.BizException;
import com.stellariver.milky.demo.adapter.controller.ItemController;
import com.stellariver.milky.spring.partner.limit.RateLimitConfigTunnel;
import com.stellariver.milky.spring.partner.limit.RateLimitSupport;
import lombok.CustomLog;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@CustomLog
@SpringBootTest
public class RateLimitTest {

    @MockBean
    private RateLimitConfigTunnel rateLimitConfigTunnel;
    
    @Autowired
    private ItemController itemController;

    @Autowired
    private RateLimitSupport rateLimitSupport;
    
    
    @Test
    @SneakyThrows
    public void rateLimitTest() {
        Mockito.when(rateLimitConfigTunnel.qps(Mockito.anyString())).thenReturn(100);
        Mockito.when(rateLimitConfigTunnel.key(Mockito.any())).thenReturn("test");
        int generalRequestCount1 = 100;
        for (int i = 0; i < generalRequestCount1; i++) {
            itemController.testRateLimit();
            Thread.sleep(20);
        }

        Throwable t = null;
        int generalRequestCount2 = 300;
        try {
            for (int i = 0; i < generalRequestCount2; i++) {
                itemController.testRateLimit();
                Thread.sleep(1);
            }
        } catch (Throwable throwable) {
            t = throwable;
        }
        Assertions.assertNotNull(t);
        Assertions.assertTrue(t instanceof BizException);

        Mockito.when(rateLimitConfigTunnel.qps(Mockito.anyString())).thenReturn(3000);
        Mockito.when(rateLimitConfigTunnel.key(Mockito.any())).thenReturn("test");
        rateLimitSupport.resetRateLimiterContainer();
        t = null;
        try {
            for (int i = 0; i < generalRequestCount2; i++) {
                itemController.testRateLimit();
                Thread.sleep(1);
            }
        } catch (Throwable throwable) {
            t = throwable;
        }
        Assertions.assertNull(t);
    }


}
