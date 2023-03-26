package com.stellariver.milky.demo.domain.service;

import lombok.CustomLog;
import org.springframework.stereotype.Service;

/**
 * @author houchuang
 */
@Service
@CustomLog
public class MqService {

    public void sendMessage(ItemCreatedMessage message) {
        log.arg0(message).info("ItemCreatedMessage");
    }

    public void sendMessage(ItemTitleUpdatedMessage message) {
        log.arg0(message).info("ItemTitleUpdatedMessage");
    }

    public void sendMessage(ItemAmountUpdatedMessage message) {
        log.arg0(message).info("ItemAmountUpdatedMessage");
    }

}
