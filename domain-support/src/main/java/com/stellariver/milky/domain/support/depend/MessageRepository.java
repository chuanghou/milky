package com.stellariver.milky.domain.support.depend;

import com.stellariver.milky.domain.support.base.Message;

import java.util.List;
import java.util.Map;

public interface MessageRepository {


    void batchInsert(List<Message> messages, Map<String, Object> metaData);

    List<Message> query(MessageQuery query);

}
