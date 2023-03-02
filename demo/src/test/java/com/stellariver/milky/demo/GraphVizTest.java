package com.stellariver.milky.demo;

import com.stellariver.milky.common.tool.state.machine.StateMachine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.io.IOException;


//http://viz-js.com/
//# http://www.graphviz.org/content/cluster
//
//        digraph fsm {
//        "a" -> "a" [label= "Event1, c:condition, r: runner1"]
//        "a" -> "b" [label= "Event2"]
//        "b" -> "c" [label= "Event3"]
//        "b" -> "d" [label= "Event4"]
//        "c" -> "a" [label= "Event5"]
//        }

public class GraphVizTest {

    @Test
    public void test() throws IOException {
        String data = "# http://www.graphviz.org/content/cluster\n" +
                "\n" +
                "        digraph fsm {\n" +
                "        \"a\" -> \"a\" [label= \"Event1, c:condition, r: runner1\"]\n" +
                "        \"a\" -> \"b\" [label= \"Event2\"]\n" +
                "        \"b\" -> \"c\" [label= \"Event3\"]\n" +
                "        \"b\" -> \"d\" [label= \"Event4\"]\n" +
                "        \"c\" -> \"a\" [label= \"Event5\"]\n" +
                "        }";

        StateMachine<String, String> machine = StateMachine.buildStateMachine(data);

        String fire = machine.fire("a", "Event2");
        Assertions.assertEquals(fire, "b");

        fire = machine.fire("b", "Event2");
        Assertions.assertNull(fire);
    }

}
