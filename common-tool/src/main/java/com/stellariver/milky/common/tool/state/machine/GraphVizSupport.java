package com.stellariver.milky.common.tool.state.machine;

import com.stellariver.milky.common.tool.common.BeanUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GraphVizSupport {

    static private Pattern patternS = Pattern.compile("\\\"\\w+\\\"\\s*->");
    static private Pattern patternT = Pattern.compile("->\\s*\\\"\\w+\\\"");
    static private Pattern patternL = Pattern.compile("\\[\\s*label\\s*=\\s*\\\".+\\\"\\]");
    static private Pattern patternE = Pattern.compile("\\\"\\s*\\w+(,|\\\")");
    static private Pattern patternC = Pattern.compile("c\\s*\\:\\s*\\w+\\s*(\\,|\\\")");
    static private Pattern patternR = Pattern.compile("r\\s*\\:\\s*\\w+\\s*(\\,|\\\")");

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
                throw new RuntimeException("please paste\n" + dot + "\nto http://viz-js.com/ to verify the validation!");
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
