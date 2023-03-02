package com.stellariver.milky.demo;

import com.stellariver.milky.common.tool.state.machine.StateMachine;
import com.stellariver.milky.common.tool.state.machine.Transition;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GraphVizTest {

    @Test
    public void test() throws IOException {
        ClassPathResource cpr = new ClassPathResource("a.txt");
        byte[] bdata = FileCopyUtils.copyToByteArray(cpr.getInputStream());
        String data = new String(bdata, StandardCharsets.UTF_8);

        // 按指定模式在字符串查找
        String patternSource = "\\\"\\w+\\\"\\s*->";


        String patternTarget = "->\\s*\\\"\\w+\\\"";


        String patternEvent = "\\[\\s*label\\s*=\\s*\\\"\\s*\\w+\\s*\\\"\\s*\\]";



        Pattern patternS = Pattern.compile(patternSource);
        Pattern patternT = Pattern.compile(patternTarget);
        Pattern patternE = Pattern.compile(patternEvent);
        Matcher matcherS = patternS.matcher(data);
        Matcher matcherT = patternT.matcher(data);
        Matcher matcherE = patternE.matcher(data);

        List<Transition<String, String>> transitions = new ArrayList<>();
        while (true) {
            boolean s = matcherS.find();
            boolean t = matcherT.find();
            boolean e = matcherE.find();

            if (!s && !t && !e) {
                break;
            }
            if (s && t && e) {
                String sourceTemp = matcherS.group();
                String source = extract(sourceTemp);
                String targetTemp = matcherT.group();
                String target = extract(targetTemp);
                String eventTemp = matcherE.group();
                String event = extract(eventTemp);
                Transition<String, String> transition = Transition.<String, String>builder()
                        .source(source).event(event).target(target).build();
                transitions.add(transition);
            }
        }

        StateMachine<String, String> machine = new StateMachine<>(transitions);
        String fire = machine.fire("a", "Event2");
        System.out.println(fire);

    }

    static public Pattern quotes = Pattern.compile("\\\"\\s*\\w+\\s*\\\"");
    private String extract(String sourceTemp) {
        Matcher matcher = quotes.matcher(sourceTemp);
        if (matcher.find()) {
            String group = matcher.group();
            return group.substring(1, group.length() - 1).trim();
        } else {
            throw new RuntimeException(sourceTemp);
        }
    }
}
