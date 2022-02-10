package com.echobaba.milky.domain.support.base;

import com.alibaba.c2m.milky.domain.support.context.Context;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.Date;
import java.util.UUID;

@Data
@SuperBuilder
public abstract class Message {

    protected String id;

    protected Context context;

    protected Date gmtCreate;

    public abstract String getAggregationId();

    public abstract void setAggregationId(String aggregationId);

    public Message() {
        this.id = UUID.randomUUID().toString();
        this.gmtCreate = new Date();
    }
}
