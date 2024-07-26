package com.stellariver.milky.demo;

import com.stellariver.milky.demo.adapter.sse.DelayGroupId;
import com.stellariver.milky.demo.adapter.sse.DelayProcessor;
import com.stellariver.milky.demo.adapter.sse.DelayedMessageConsumer;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class DelayProcessorTest {

    @AllArgsConstructor
    static public class Msg implements DelayGroupId {

        String id;

        @Override
        public String groupId() {
            return id;
        }
    }


    @Test
    public void testDelay() throws InterruptedException {
        DelayProcessor<Msg> msgDelayProcessor = new DelayProcessor<>(new DelayedMessageConsumer<Msg>() {
            @Override
            public void consume(Msg msg) {
                System.out.println(new Date() + "msg" + msg.id);
            }
        });
        new Thread(msgDelayProcessor).start();
        msgDelayProcessor.add(new Msg("1"), System.currentTimeMillis() + 1000);
        msgDelayProcessor.add(new Msg("2"), System.currentTimeMillis() + 2000);
        msgDelayProcessor.add(new Msg("3"), System.currentTimeMillis() + 3000);
        Thread.sleep(5_000);
    }


}
