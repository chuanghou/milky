package com.stellariver.milky.spring.partner.wire;

import com.stellariver.milky.common.tool.wire.StaticWireSupport;
import lombok.AllArgsConstructor;
import org.reflections.Reflections;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;

@AllArgsConstructor
public class StaticWireApplicationRunner implements ApplicationRunner, Ordered {

    private Reflections reflections;

    @Override
    public void run(ApplicationArguments args) {
        StaticWireSupport.wire(reflections);
    }

    public void close() {
        StaticWireSupport.unWire(reflections);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

}
