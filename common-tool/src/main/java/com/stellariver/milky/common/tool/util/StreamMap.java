package com.stellariver.milky.common.tool.util;

import java.util.HashMap;
import java.util.Map;

public class StreamMap<K, V>{

    private final Map<K, V> map = new HashMap<>();

    public StreamMap<K, V> put(K k, V v) {
        map.put(k, v);
        return this;
    }

    public StreamMap<K, V> put(Map<K, V> map) {
        this.map.putAll(map);
        return this;
    }

    static public <K, V> StreamMap<K, V> init() {
        return new StreamMap<>();
    }

    public Map<K, V> getMap() {
        return map;
    }



}
