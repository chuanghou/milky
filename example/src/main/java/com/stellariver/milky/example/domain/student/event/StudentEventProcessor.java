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
        System.out.println(1);
    }

    @EventHandler(order = 2, type = HandlerTypeEnum.ASYNC)
    public void handle2(NameChangeEvent event, Context context) {

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(2);
    }

}
