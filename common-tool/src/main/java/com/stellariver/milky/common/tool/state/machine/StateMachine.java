package com.stellariver.milky.common.tool.state.machine;

import com.stellariver.milky.common.tool.common.BeanUtil;
import lombok.NonNull;

import javax.annotation.Nullable;
import java.util.*;
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

    public StateMachine(@NonNull List<Transition<State, Event>> transitionList) {
        transitionList.forEach(t -> {
            State source = t.getSource();
            Event event = t.getEvent();
            State target = t.getTarget();
            Condition<State, Event> condition = t.getCondition();
            Runner<State, Event> runner = t.getRunner();
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

    static final private Map<String, Object> emptyMap = new HashMap<>();


    public State fire(State source, Event event) {
        return fire(source, event, emptyMap);
    }

    /**
     *
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

    public static StateMachine<String, String> fromGraphViz(String dot) {

        Matcher matcherSource = patternS.matcher(dot);
        Matcher matcherTarget = patternT.matcher(dot);
        Matcher matcherLable = patternL.matcher(dot);

        List<Transition<String, String>> transitions = new ArrayList<>();
        while (true) {
            boolean s = matcherSource.find();
            boolean t = matcherTarget.find();
            boolean l = matcherLable.find();

            if (!s && !t && !l) {
                break;
            } else if (s && t && l) {
                String source = extractByQuotes(matcherSource.group());
                String target = extractByQuotes(matcherTarget.group());
                String label = matcherLable.group();

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
                    event = group.substring(group.indexOf("\"") + 1, group.length() - 1).trim();
                } else {
                    throw new RuntimeException(label);
                }

                boolean avilableCondition = condition == null || BeanUtil.getBeanLoader() == null;
                Object conditionBean = avilableCondition ? null : BeanUtil.getBean(condition);
                boolean avilableAction = condition == null || BeanUtil.getBeanLoader() == null;
                Object runnerBean = avilableAction ? null : BeanUtil.getBean(runner);

                Transition<String, String> transition = Transition.<String, String>builder()
                        .source(source).event(event).target(target)
                        .condition((Condition<String, String>) conditionBean)
                        .runner((Runner<String, String>) runnerBean)
                        .build();

                transitions.add(transition);
            } else {
                throw new RuntimeException("please paste" + dot + "to http://viz-js.com/ to verify the validation!");
            }
        }
        return new StateMachine<>(transitions);
    }

    static private String extractByQuotes(String str) {
        int leftIndex = str.indexOf("\"");
        int rightIndex = str.lastIndexOf("\"");
        if (rightIndex - leftIndex >= 2) {
            return str.substring(leftIndex + 1, rightIndex).trim();
        } else {
            throw new RuntimeException(str + ", left: " + "\"" + ", right: " + "\"");
        }
    }

}
