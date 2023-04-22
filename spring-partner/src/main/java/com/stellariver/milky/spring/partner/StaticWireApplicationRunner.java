package com.stellariver.milky.spring.partner;

import com.stellariver.milky.common.tool.wire.StaticWireSupport;
import lombok.AllArgsConstructor;
import org.reflections.Reflections;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

@AllArgsConstructor
public class StaticWireApplicationRunner implements ApplicationRunner {

    private Reflections reflections;

    @Override
    public void run(ApplicationArguments args) {
        StaticWireSupport.wire(reflections);
    }

    public void close() {
        StaticWireSupport.unWire(reflections);
    }

}
