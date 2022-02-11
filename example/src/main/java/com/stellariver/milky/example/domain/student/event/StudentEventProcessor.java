package com.stellariver.milky.example.domain.student.event;

import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.event.EventHandler;
import com.stellariver.milky.domain.support.event.EventProcessor;
import com.stellariver.milky.domain.support.event.HandlerTypeEnum;
import org.springframework.stereotype.Service;

@Service
public class StudentEventProcessor implements EventProcessor {

    @EventHandler(order = 1)
    public void handle1(NameChangeEvent event, Context context) {
        System.out.println(event);
    }
}
