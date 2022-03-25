package com.stellariver.milky.domain.support.command;

import com.stellariver.milky.domain.support.base.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
abstract public class Command extends Message {

    public boolean allowAsync() {
        return true;
    }

    public int lockExpireMils() {
        return 3000;
    }

    public int retryTimes() {
        return 3;
    }

    public long[] violationRandomSleepRange() {
        return new long[]{100L, 300L};
    }

}
