package com.stellariver.milky.common.tool.state.machine;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.annotation.Nullable;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Action<State, Event> {

    State target;

    @Nullable
    Condition<State, Event> condition;

    @Nullable
    Runner<State, Event> runner;

}
