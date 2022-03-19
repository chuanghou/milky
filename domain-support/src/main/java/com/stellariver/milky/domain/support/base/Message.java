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

    protected String identifier;

    protected Context context;

    protected Date gmtCreate;

    @Builder.Default
    protected Map<String, Object> extensions = new HashMap<>();

    public abstract String getAggregationId();

    public Message() {
        this.identifier = getAggregationId() + "_" + UUID.randomUUID();
        this.gmtCreate = new Date();
    }

    public void put(String extensionKey, Object extensionValue) {
        extensions.put(extensionKey, extensionValue);
    }

}
