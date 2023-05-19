package com.stellariver.milky.common.tool.state.machine;

import lombok.NonNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This a very simple state machine implementation, I have ever use squirrel state machine and spring state machine,
 * these are too complicated, then I found Cola State machine, the reason why this state machine occur is the author has
 * same feeling with me, but for me cola state machine is also too complicated
 * @param <State> the type of state
 * @param <Event> the type of event
 */

@SuppressWarnings("all")
public class StateMachine<State, Event> {

    private final Map<State, Map<Event, Action<State, Event>>> transitions = new HashMap<>();

    public StateMachine(@NonNull List<Transition<State, Event>> transitions) {
        transitions.forEach(t -> {
            State source = t.getSource();
            Event event = t.getEvent();
            State target = t.getTarget();
            Condition<State, Event> condition = t.getCondition();
            Runner<State, Event> runner = t.getRunner();
            boolean b = source == null || event == null || target == null;
            if (b) {
                throw new IllegalArgumentException(transitions.toString());
            }
            Map<Event, Action<State, Event>> map = this.transitions.computeIfAbsent(source, k -> new HashMap<>());
            Action<State, Event> action = new Action<>(target, condition, runner);
            Action<State, Event> put = map.put(event, action);
            if (put != null) {
                throw new RepeatStateConfigException(transitions.toString());
            }
        });
    }

    static final private Map<String, Object> emptyMap = new HashMap<>();

    public State fire(State source, Event event) {
        return fire(source, event, emptyMap);
    }

    /**
     * @param source the source state
     * @param event the event on this state machine
     * @return the target state, if the target state is null, mean the source state did not accept this event when
     * the source state is the param
     */

    @Nullable
    public State fire(@NonNull State source, @NonNull Event event, Map<String, Object> context) {
        context = Optional.ofNullable(context).orElse(emptyMap);
        Map<Event, Action<State, Event>> map = transitions.get(source);
        if (map == null) {
            return null;
        }
        Action<State, Event> action = map.get(event);

        if (action == null) {
            return null;
        }

        Condition<State, Event> condition = action.getCondition();
        boolean b = condition == null || condition.test(source, event, context);
        if (b) {
            if (action.getRunner() != null) {
                action.getRunner().run(source, event, action.getTarget(), context);
            }
            return action.getTarget();
        } else {
            return null;
        }
    }

}
