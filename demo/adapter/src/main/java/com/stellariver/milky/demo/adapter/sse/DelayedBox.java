package com.stellariver.milky.demo.adapter.sse;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DelayedBox<Message extends DelayGroupId> implements Delayed {

    @Getter
    Message message;

    long maxExpiration;


    @Override
    public long getDelay(@NonNull TimeUnit unit) {
        long duration = System.currentTimeMillis() - maxExpiration;
        return unit.convert(duration, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(@NonNull Delayed o) {
        return (int) (this.getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
    }

}
