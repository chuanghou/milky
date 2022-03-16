package com.stellariver.milky.domain.support.command;

import com.stellariver.milky.domain.support.base.Message;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
abstract public class Command extends Message {

    public Command() {
        super();
    }

    public boolean allowAsync() {
        return true;
    }

    public int lockExpireSeconds() {
        return 3;
    }

    public int retryTimes() {
        return 3;
    }

    public int[] violationRandomSleep() {
        return new int[]{100, 300};
    }

}
