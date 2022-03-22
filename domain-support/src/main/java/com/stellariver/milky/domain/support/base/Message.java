package com.stellariver.milky.domain.support.base;

import com.stellariver.milky.domain.support.context.Context;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@SuperBuilder
@AllArgsConstructor
public abstract class Message {

    protected String id;

    protected String triggerId;

    protected String aggregateId;

    public abstract String initIdentifier();

    public abstract String initTriggerId();

    public abstract String initAggregateId();

    public Message() {
        this.id = initIdentifier();
        this.aggregateId = initAggregateId();
        this.triggerId = initTriggerId();
    }


}
