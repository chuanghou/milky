package com.stellariver.milky.domain.support.context;


import com.stellariver.milky.common.tool.common.SysException;
import com.stellariver.milky.domain.support.base.Message;
import com.stellariver.milky.domain.support.event.Event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Context{

    private final Map<String, Object> metaData = new HashMap<>();

    private final Map<String, Object> dependencies = new HashMap<>();

    private final List<Event> events = new ArrayList<>();

    private final List<Event> processedEvents = new ArrayList<>();

    private final List<Message> recordedMessages = new ArrayList<>();

    public Object getMetaData(String key) {
        return metaData.get(key);
    }

    public void putMetaData(String key, Object value) {
        metaData.put(key, value);
    }

    public Map<String, Object> getMetaData() {
        return new HashMap<>(metaData);
    }

    public Object getDependency(String key) {
        return dependencies.get(key);
    }

    public void putDependency(String key, Object value) {
        dependencies.put(key, value);
    }

    public void clearDependencies() {
        dependencies.clear();
    }

    public void publish(@Nonnull Event event) {
        SysException.nullThrow(event);
        events.add(event);
        recordMessage(event);
    }

    public void recordMessage(@Nonnull Message message) {
        SysException.nullThrow(message);
        recordedMessages.add(message);
    }

    public List<Message> getRecordedMessages() {
        return recordedMessages;
    }

    @Nullable
    public Event popEvent() {
        Event event = null;
        if (!events.isEmpty()) {
            event = events.remove(0);
            processedEvents.add(event);
        }
        return event;
    }

    public List<Event> getProcessedEvents() {
        return processedEvents;
    }

    public List<Event> peekEvents() {
        return new ArrayList<>(events);
    }

    public Event peekEvent() {
        return events.get(0);
    }



}




