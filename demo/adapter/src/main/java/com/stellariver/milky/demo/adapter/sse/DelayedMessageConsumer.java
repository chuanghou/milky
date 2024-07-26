package com.stellariver.milky.demo.adapter.sse;

public interface DelayedMessageConsumer<Message extends DelayGroupId> {


    void consume(Message message);

}
