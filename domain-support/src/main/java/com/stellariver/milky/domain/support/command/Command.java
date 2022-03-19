package com.stellariver.milky.domain.support.command;

import com.stellariver.milky.domain.support.base.Message;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
abstract public class Command extends Message {

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
