package com.stellariver.milky.domain.support.context;


import com.stellariver.milky.common.tool.common.SysException;
import com.stellariver.milky.common.tool.util.StreamMap;
import com.stellariver.milky.domain.support.ErrorEnum;
import com.stellariver.milky.domain.support.base.*;
import com.stellariver.milky.domain.support.dependency.IdBuilder;
import com.stellariver.milky.domain.support.event.Event;
import com.stellariver.milky.domain.support.util.BeanUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Context{

    private Long invocationId;

    private final Map<NameType<?>, Object> parameters = new HashMap<>();

    private final Map<NameType<?>, Object> metaData = new HashMap<>();

    private final Map<NameType<?>, Object> dependencies = new HashMap<>();

    private final List<Event> events = new ArrayList<>();

    private final List<Event> finalRouteEvents = new ArrayList<>();

    private final List<MessageRecord> messageRecords = new ArrayList<>();

    public Map<NameType<?>, Object> getMetaData() {
        return StreamMap.init(metaData).getMap();
    }

    public Object peekParameter(NameType<?> key) {
        return parameters.get(key);
    }

    public Map<NameType<?>, Object> getParameters() {
        return StreamMap.init(parameters).getMap();
    }


    public void putMetaData(NameType<?> key, Object value) {
        SysException.trueThrowGet(metaData.containsKey(key), () -> ErrorEnum.META_DATA_DUPLICATE_KEY.message(key));
        metaData.put(key, value);
    }

    public void replaceMetaData(NameType<?> key, Object value) {
        metaData.put(key, value);
    }

    public Object getDependency(String key) {
        return dependencies.get(key);
    }

    public Map<NameType<?>, Object> getDependencies() {
        return dependencies;
    }

    public void putDependency(String key, Object value) {
        dependencies.put(key, value);
    }

    public void clearDependencies() {
        dependencies.clear();
    }

    public void publish(@Nonnull Event event) {
        SysException.anyNullThrow(event);
        events.add(0, event);
    }

    public void recordCommand(@Nonnull CommandRecord commandRecord) {
        SysException.anyNullThrow(commandRecord);
        messageRecords.add(commandRecord);
    }

    public void recordEvent(@Nonnull EventRecord eventRecord) {
        SysException.anyNullThrow(eventRecord);
        messageRecords.add(eventRecord);
    }

    public List<MessageRecord> getMessageRecords() {
        return messageRecords;
    }

    @Nullable
    public List<Event> popEvents() {
        List<Event> popEvents = new ArrayList<>(this.events);
        events.clear();
        return popEvents;
    }

    public List<Event> getFinalRouteEvents() {
        return finalRouteEvents;
    }

    public List<Event> peekEvents() {
        return new ArrayList<>(events);
    }

    public static Context fromParameters(Map<String, Object> parameters) {
        Context context = new Context();
        context.invocationId = BeanUtil.getBean(IdBuilder.class).build();
        context.parameters.putAll(parameters);
        return context;
    }

    public Long getInvocationId() {
        return invocationId;
    }

}




