package com.stellariver.milky.common.tool.state.machine;

import com.stellariver.milky.common.tool.common.TriConsumer;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * This a very simple state machine implementation, I have ever use squirrel state machine and spring state machine,
 * these are too complicated, then I found Cola State machine, the reason why this state machine occur is the author has
 * same feeling with me, but for me cola state machine is also too complicated
 * @param <State> the type of state
 * @param <Event> the type of event
 */

public class StateMachine<State, Event> {

    private final Map<State, Map<Event, Action<State, Event>>> transitions = new HashMap<>();

    public StateMachine(List<Transition<State, Event>> transitionList) {

        if (transitionList == null) {
            throw new IllegalArgumentException("parameter is null");
        }

        transitionList.forEach(t -> {
            State source = t.getSource();
            Event event = t.getEvent();
            State target = t.getTarget();
            BiPredicate<State, Event> condition = t.getCondition();
            TriConsumer<State, Event, State> runner = t.getAction();
            boolean b = source == null || event == null || target == null;
            if (b) {
                throw new IllegalArgumentException(transitionList.toString());
            }
            Map<Event, Action<State, Event>> map = transitions.computeIfAbsent(source, k -> new HashMap<>());
            Action<State, Event> action = new Action<>(source, condition, runner);
            Action<State, Event> put = map.put(event, action);
            if (put != null) {
                throw new RepeatStateConfigException(transitionList.toString());
            }
        });

    }

    /**
     *
     * @param source the source state
     * @param event the event on this state machine
     * @return the target state, if the target state is null, mean the source state did not accept this event when
     * the source state is the param
     */

    @Nullable
    public State fire(State source, Event event) {
        Map<Event, Action<State, Event>> map = transitions.get(source);
        if (map == null) {
            return null;
        }
        Action<State, Event> action = map.get(event);

        if (action == null) {
            return null;
        }

        BiPredicate<State, Event> condition = action.getCondition();
        boolean b = condition == null || condition.test(source, event);
        if (b) {
            if (action.getRunner() != null) {
                action.getRunner().accept(source, event, action.getTarget());
            }
            return action.getTarget();
        } else {
            return null;
        }
    }

}
