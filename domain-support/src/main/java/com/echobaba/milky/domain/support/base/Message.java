package com.echobaba.milky.domain.support.base;

import com.echobaba.milky.domain.support.context.Context;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.Date;
import java.util.UUID;

@Data
@SuperBuilder
public abstract class Message {

    protected String identifier;

    protected Context context;

    protected Date gmtCreate;

    public abstract String getAggregationId();

    public abstract void setAggregationId(String aggregationId);

    public Message() {
        this.identifier = UUID.randomUUID().toString();
        this.gmtCreate = new Date();
    }
}
