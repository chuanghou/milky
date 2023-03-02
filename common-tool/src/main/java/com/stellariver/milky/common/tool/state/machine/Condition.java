package com.stellariver.milky.common.tool.state.machine;

import java.util.Map;

@FunctionalInterface
public interface Condition<State, Event> {

    boolean test(State source, Event event, Map<String, Object> context);

}
