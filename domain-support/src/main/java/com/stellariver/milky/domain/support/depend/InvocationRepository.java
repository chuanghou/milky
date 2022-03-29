package com.stellariver.milky.domain.support.depend;

import com.stellariver.milky.domain.support.Invocation;
import com.stellariver.milky.domain.support.base.Message;

import java.util.List;
import java.util.Map;

public interface InvocationRepository {


    void insert(Invocation messages, Map<String, Object> metaData);

    List<Invocation> query(MessageQuery query);

}
