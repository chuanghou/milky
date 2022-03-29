package com.stellariver.milky.domain.support.dependency;

import com.stellariver.milky.domain.support.invocation.Invocation;
import com.stellariver.milky.domain.support.base.MessageQuery;

import java.util.List;
import java.util.Map;

public interface InvocationRepository {


    void insert(Invocation messages, Map<String, Object> metaData);

    List<Invocation> query(MessageQuery query);

}
