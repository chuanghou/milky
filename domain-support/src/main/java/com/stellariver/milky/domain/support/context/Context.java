package com.stellariver.milky.domain.support.context;


import com.stellariver.milky.common.tool.common.SysException;
import com.stellariver.milky.domain.support.Invocation;
import com.stellariver.milky.domain.support.event.Event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Context{

    private Invocation invocation;

    private final Map<String, Object> metaData = new HashMap<>();

    private final List<Event> events = new ArrayList<>();

    private final List<Event> processedEvents = new ArrayList<>();

    public Object getMetaData(String key) {
        return metaData.get(key);
    }

    public void putMetaData(String key, Object value) {
        metaData.put(key, value);
    }

    public void pushEvent(@Nonnull Event event) {
        SysException.nullThrow(event);
        events.add(event);
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

    public Context withInvocation(Invocation invocation) {
        this.invocation = invocation;
        return this;
    }

    public Invocation getInvocation() {
       return invocation;
    }

    public List<Event> peekEvents() {
        return new ArrayList<>(events);
    }

}




