package com.stellariver.milky.starter.demo.starter;

import com.stellariver.milky.common.tool.common.Clock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClockTest {

    @Test
    public void testClock() {
        Assertions.assertNotEquals(Clock.today(), 0);
    }
}
