package com.stellariver.milky.common.tool.state.machine;

import com.stellariver.milky.common.tool.common.TriConsumer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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


    static private Pattern patternS = Pattern.compile("\\\"\\w+\\\"\\s*->");
    static private Pattern patternT = Pattern.compile("->\\s*\\\"\\w+\\\"");
    static private Pattern patternL = Pattern.compile("\\[\\s*label\\s*=\\s*\\\".+\\\"\\]");
    static private Pattern patternE = Pattern.compile("\\\"\\s*\\w+(,|\\\")");
    static private Pattern patternC = Pattern.compile("c\\s*\\:\\s*\\w+\\s*(\\,|\\\")");
    static private Pattern patternR = Pattern.compile("r\\s*\\:\\s*\\w+\\s*(\\,|\\\")");

    public static StateMachine<String, String> buildStateMachine(String dot) {

        Matcher matcherS = patternS.matcher(dot);
        Matcher matcherT = patternT.matcher(dot);
        Matcher matcherL = patternL.matcher(dot);

        List<Transition<String, String>> transitions = new ArrayList<>();
        while (true) {
            boolean s = matcherS.find();
            boolean t = matcherT.find();
            boolean l = matcherL.find();

            if (!s && !t && !l) {
                break;
            }
            if (s && t && l) {
                String source = extractByDelimiter(matcherS.group());
                String target = extractByDelimiter(matcherT.group());
                String label = matcherL.group();

                String condition = null;
                Matcher matcherC = patternC.matcher(label);
                if (matcherC.find()) {
                    String group = matcherC.group();
                    condition = group.substring(group.indexOf(":") + 1, group.length() - 1).trim();
                }

                String runner = null;
                Matcher matcherR = patternR.matcher(label);
                if (matcherR.find()) {
                    String group = matcherR.group();
                    runner = group.substring(group.indexOf(":") + 1, group.length() - 1).trim();
                }

                String event;
                Matcher matcherE = patternE.matcher(label);
                if (matcherE.find()) {
                    String group = matcherE.group();
                    event = group.substring(1, group.length() - 1).trim();
                } else {
                    throw new RuntimeException(label);
                }

//                @SuppressWarnings("unchecked")
//                BiPredicate<String, String> conditionBean = BeanUtil.getBeanLoader() == null || condition == null ?
//                        null : (BiPredicate<String, String>) BeanUtil.getBean(condition);
//                @SuppressWarnings("unchecked")
//                TriConsumer<String, String, String> actionBean = BeanUtil.getBeanLoader() == null || runner == null ?
//                        null : (TriConsumer<String, String, String>) BeanUtil.getBean(runner);

                Transition<String, String> transition = Transition.<String, String>builder()
                        .source(source).event(event).target(target)
//                        .condition(conditionBean)
//                        .action(actionBean)
                        .build();

                transitions.add(transition);
            } else {
                throw new RuntimeException(dot);
            }
        }
        return new StateMachine<>(transitions);
    }

    static private String extractByDelimiter(String str) {
        int leftIndex = str.indexOf("\"");
        int rightIndex = str.lastIndexOf("\"");
        if (rightIndex - leftIndex >= 2) {
            return str.substring(leftIndex + 1, rightIndex).trim();
        } else {
            throw new RuntimeException(str + ", left: " + "\"" + ", right: " + "\"");
        }
    }

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
            Action<State, Event> action = new Action<>(target, condition, runner);
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
