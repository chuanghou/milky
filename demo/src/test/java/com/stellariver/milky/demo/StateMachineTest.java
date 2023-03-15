package com.stellariver.milky.demo;

import com.stellariver.milky.common.tool.state.machine.GraphVizSupport;
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
            new StateMachine<>(Arrays.asList(t0, t00));
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
                .condition((s, e, c) -> false)
                .build();

        AtomicBoolean runnerWorked = new AtomicBoolean(false);
        Transition<State, Event> t4 = Transition.<State, Event>builder()
                .source(State.B).event(Event.BC).target(State.C)
                .condition((s, e, c) -> true)
                .runner((s, e, t, c) -> runnerWorked.set(true))
                .build();

        machine = new StateMachine<>(Arrays.asList(t3, t4));

        target = machine.fire(State.A, Event.AB);
        Assertions.assertNull(target);

        target = machine.fire(State.B, Event.BC);
        Assertions.assertEquals(target, State.C);
        Assertions.assertTrue(runnerWorked.get());

    }


    /**
     * Graphviz comes from bell lab and is a useful tool to make some graph
     * This state machine accept a graphviz supportable state transfer graph as input to get a state machine
     * example:
     * <pre>
     * digraph fsm {
     *         "a" -> "a" [label= "Event1, c:condition, r: runner1"]
     *         "a" -> "b" [label= "Event2"]
     *         "b" -> "c" [label= "Event3"]
     *         "b" -> "d" [label= "Event4"]
     *         "c" -> "a" [label= "Event5"]
     * }</pre>
     * paste above code into <a href="http://viz-js.com/">viz</a> you can see the state machine result
     * above test verify the state machine graph
     */

    @Test
    public void test() {
        String data = "  digraph fsm {\n" +
                "          \"a\" -> \"a\" [label= \"Event1, c:condition, r: runner1\"]\n" +
                "          \"a\" -> \"b\" [label= \"Event2\"]\n" +
                "          \"b\" -> \"c\" [label= \"Event3\"]\n" +
                "          \"b\" -> \"d\" [label= \"Event4\"]\n" +
                "          \"c\" -> \"a\" [label= \"Event5\"]\n" +
                "  }";

        StateMachine<String, String> machine = GraphVizSupport.fromGraphViz(data);

        String fire = machine.fire("a", "Event2");
        Assertions.assertEquals(fire, "b");

        fire = machine.fire("b", "Event2");
        Assertions.assertNull(fire);
    }
}
