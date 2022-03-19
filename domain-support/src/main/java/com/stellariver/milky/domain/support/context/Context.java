package com.stellariver.milky.domain.support.context;


import com.stellariver.milky.domain.support.base.AggregateRoot;
import com.stellariver.milky.domain.support.event.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Context{

    private AggregateRoot aggregateRoot;

    private final Map<String, Object> metaData = new HashMap<>();

    public List<Event> events = new ArrayList<>();

    public void put(String key, Object value) {
        metaData.put(key, value);
    }

    public Object get(String key) {
        return metaData.get(key);
    }

    public void addEvent(Event event) {
        events.add(event);
    }

    public void setAggregateRoot(AggregateRoot aggregateRoot) {
        this.aggregateRoot = aggregateRoot;
    }

    public AggregateRoot getAggregateRoot() {
        return aggregateRoot;
    }

    public static Context build() {
        return new Context();
    }

}




