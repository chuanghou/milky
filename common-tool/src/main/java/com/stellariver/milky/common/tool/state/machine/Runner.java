package com.stellariver.milky.common.tool.state.machine;

import java.util.Map;

@FunctionalInterface
public interface Runner<State, Event> {

    void run(State source, Event event, State target, Map<String, Object> context);

}
