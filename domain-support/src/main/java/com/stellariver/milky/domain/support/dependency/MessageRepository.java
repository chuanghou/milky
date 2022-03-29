package com.stellariver.milky.domain.support.dependency;

import com.stellariver.milky.domain.support.base.Message;
import com.stellariver.milky.domain.support.base.MessageQuery;

import java.util.List;
import java.util.Map;

public interface MessageRepository {


    void batchInsert(List<Message> messages, Map<String, Object> metaData);

    List<Message> query(MessageQuery query);

}
