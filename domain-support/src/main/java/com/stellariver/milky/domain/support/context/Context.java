package com.stellariver.milky.domain.support.context;


import com.stellariver.milky.domain.support.base.AggregateRoot;
import com.stellariver.milky.domain.support.event.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Context{

    private final Map<String, Object> metaData = new HashMap<>();

    private final List<Event> events = new ArrayList<>();

    public Object get(String key) {
        return metaData.get(key);
    }

    public void put(String key, Object value) {
        metaData.put(key, value);
    }

    public void addEvent(Event event) {
        events.add(event);
    }

    public List<Event> getEvents() {
        return events;
    }

}




