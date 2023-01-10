package com.stellariver.milky.common.tool.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author houchuang
 */
public class StreamMap<K, V> {

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

    static public <K, V> StreamMap<K, V> init(K k, V v) {
        StreamMap<K, V> map = new StreamMap<>();
        map.put(k, v);
        return map;
    }

    static public <K, V> StreamMap<K, V> init(Map<K, V> map) {
        StreamMap<K, V> streamMap = new StreamMap<>();
        streamMap.map.putAll(map);
        return streamMap;
    }

    public Map<K, V> getMap() {
        return map;
    }



}
