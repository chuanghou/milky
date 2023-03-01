package com.stellariver.milky.demo;

import com.stellariver.milky.common.tool.state.machine.RepeatStateConfigException;
import com.stellariver.milky.common.tool.state.machine.StateMachine;
import com.stellariver.milky.common.tool.state.machine.Transition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class StateMachineTest {

    enum State {
        A, B, C, D
    }

    enum Event {
        AB, BC
    }

    @Test
    public void testStateMachine() {
        Transition<State, Event> t0 = Transition.<State, Event>builder().source(State.A).event(Event.AB).target(State.B).build();
        Transition<State, Event> t00 = Transition.<State, Event>builder().source(State.A).event(Event.AB).target(State.C).build();


        // repeat config
        Throwable throwable = null;
        StateMachine<State, Event> machine;
        try {
            machine = new StateMachine<>(Arrays.asList(t0, t00));
        } catch (RepeatStateConfigException exception) {
            throwable = exception;
        }
        Assertions.assertNotNull(throwable);

        // normal case
        Transition<State, Event> t1 = Transition.<State, Event>builder().source(State.B).event(Event.BC).target(State.C).build();
        machine = new StateMachine<>(Arrays.asList(t0, t1));
        State target = machine.fire(State.A, Event.AB);
        Assertions.assertEquals(target, State.B);

        target = machine.fire(State.A, Event.BC);
        Assertions.assertNull(target);


        // condition and runner
        Transition<State, Event> t3 = Transition.<State, Event>builder()
                .source(State.A).event(Event.AB).target(State.B)
                .condition((s, e) -> false)
                .build();

        AtomicBoolean runnerWorked = new AtomicBoolean(false);
        Transition<State, Event> t4 = Transition.<State, Event>builder()
                .source(State.B).event(Event.BC).target(State.C)
                .condition((s, e) -> true)
                .action((s, e, t) -> runnerWorked.set(true))
                .build();

        machine = new StateMachine<>(Arrays.asList(t3, t4));

        target = machine.fire(State.A, Event.AB);
        Assertions.assertNull(target);

        target = machine.fire(State.B, Event.BC);
        Assertions.assertEquals(target, State.C);
        Assertions.assertTrue(runnerWorked.get());



    }
}
