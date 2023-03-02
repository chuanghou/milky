package com.stellariver.milky.common.tool.state.machine;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.annotation.Nullable;

@Data
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Transition<State, Event> {

    State source;

    Event event;

    State target;

    @Nullable
    Condition<State, Event> condition;

    @Nullable
    Runner<State, Event> runner;

}
