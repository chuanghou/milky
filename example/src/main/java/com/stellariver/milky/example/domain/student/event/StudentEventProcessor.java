package com.stellariver.milky.example.domain.student.event;

import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.event.EventHandler;
import com.stellariver.milky.domain.support.event.EventProcessor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
public class StudentEventProcessor extends EventProcessor {

    @EventHandler
    @Order
    public void handle(NameChangeEvent event, Context context) {
        System.out.println(event);
    }

}
