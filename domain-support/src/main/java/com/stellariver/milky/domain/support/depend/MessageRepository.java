package com.stellariver.milky.domain.support.depend;

import com.stellariver.milky.domain.support.base.Message;

import java.util.Date;
import java.util.List;

public interface MessageRepository {


    void batchInsert(List<Message> messages);

    List<Message> query(MessageQuery query);

}
