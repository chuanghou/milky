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

    @Builder.Default
    protected Map<String, Object> extensions = new HashMap<>();

    public abstract String buildIdentifier();

    public Message() {
        this.identifier = buildIdentifier();
    }

    public void put(String extensionKey, Object extensionValue) {
        extensions.put(extensionKey, extensionValue);
    }

}
