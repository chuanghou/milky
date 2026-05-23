package com.stellariver.milky.demo;

import com.stellariver.milky.common.tool.common.Clock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClockTest {

    @Test
    public void testClock() {
        Assertions.assertNotEquals(Clock.todayInteger(), 0);
    }
}
