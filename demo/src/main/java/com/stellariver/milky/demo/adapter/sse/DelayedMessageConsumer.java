package com.stellariver.milky.demo.adapter.sse;

public interface DelayedMessageConsumer<Message extends DelayGroupId> {

    /**
     * DelayProcessor need a DelayedMessageConsumer, it's running should be very fast
     * @param message the generic message that will be consumed
     */
    void consume(Message message);

}
