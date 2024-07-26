package com.stellariver.milky.demo.adapter.sse;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.DelayQueue;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DelayProcessor<Message extends DelayGroupId> implements Runnable{

    final DelayQueue<DelayedBox<Message>> delayedBoxes = new DelayQueue<>();
    final Set<String> groupIds = new ConcurrentSkipListSet<>();
    final DelayedMessageConsumer<Message> delayedMessageConsumer;

    public void add(Message message, long maxExpiration) {
        if (groupIds.contains(message.groupId())) {
            return;
        }
        DelayedBox<Message> messageDelayedBox = new DelayedBox<>(message, maxExpiration);
        delayedBoxes.offer(messageDelayedBox);
        groupIds.add(message.groupId());
    }


    @Override
    @SneakyThrows
    public void run() {
        while (true) {
            Message message = delayedBoxes.take().getMessage();
            groupIds.remove(message.groupId());
            delayedMessageConsumer.consume(message);
        }
    }



}
