package com.stellariver.milky.infrastructure.base.mq;

public enum MetaQDelayLevel {
    LEVEL_NO_DELAY(0, 0),
    LEVEL_1s(1, 1),
    LEVEL_5s(5, 2),
    LEVEL_10s(10, 3),
    LEVEL_30s(30, 4),
    LEVEL_1m(60, 5),
    LEVEL_2m(120, 6),
    LEVEL_3m(180, 7),
    LEVEL_4m(240, 8),
    LEVEL_5m(300, 9),
    LEVEL_6m(360, 10),
    LEVEL_7m(420, 11),
    LEVEL_8m(480, 12),
    LEVEL_9m(540, 13),
    LEVEL_10m(600, 14),
    LEVEL_20m(1200, 15),
    LEVEL_30m(1800, 16),
    LEVEL_1h(3600, 17),
    LEVEL_2h(7200, 18);

    private final int seconds;

    private final int levelValue;


    MetaQDelayLevel(int seconds, int levelValue) {
        this.levelValue = levelValue;
        this.seconds = seconds;
    }

    public int getSeconds() {
        return this.seconds;
    }

    public int getLevelValue() {
        return this.levelValue;
    }

}
