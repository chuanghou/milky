package com.stellariver.milky.domain.support.command;

import com.stellariver.milky.domain.support.base.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
abstract public class Command extends Message {

    public boolean allowAsync() {
        return true;
    }

    public int lockExpireMils() {
        return 5000;
    }

    public int retryTimes() {
        return 3;
    }

    public long[] violationRandomSleepRange() {
        return new long[]{100L, 300L};
    }

}
