package com.stellariver.milky.demo.adapter.sse;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Executors;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DelayProcessor<Message extends DelayGroupId> {

    final DelayQueue<DelayedBox<Message>> delayedBoxes = new DelayQueue<>();
    final Set<String> groupIds = new ConcurrentSkipListSet<>();

    @SuppressWarnings("InfiniteLoopStatement")
    public DelayProcessor(DelayedMessageConsumer<Message> delayedMessageConsumer) {
        Executors.newSingleThreadExecutor().execute(() -> {
            while (true) {
                try {
                    Message message = delayedBoxes.take().getMessage();
                    groupIds.remove(message.groupId());
                    delayedMessageConsumer.consume(message);
                } catch (Throwable throwable) {
                    log.error("consume failure", throwable);
                }
            }
        });
    }


    public void add(Message message, long maxExpiration) {
        if (groupIds.contains(message.groupId())) {
            return;
        }
        DelayedBox<Message> messageDelayedBox = new DelayedBox<>(message, maxExpiration);
        delayedBoxes.offer(messageDelayedBox);
        groupIds.add(message.groupId());
    }



}
