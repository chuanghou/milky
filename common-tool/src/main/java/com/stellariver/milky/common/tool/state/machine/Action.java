package com.stellariver.milky.common.tool.state.machine;

import com.stellariver.milky.common.tool.common.TriConsumer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.annotation.Nullable;
import java.util.function.BiPredicate;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Action<State, Event> {

    State target;

    @Nullable
    BiPredicate<State, Event> condition;

    @Nullable
    TriConsumer<State, Event, State> runner;

}
