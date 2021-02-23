package com.jinyframework.keva.server.storage;

import java.util.Map;
import java.util.Set;

public interface KevaStore {
    public Object get(String key);
    public void put(String key, Object value);
    public void putAll(Map<String, Object> m);
    public void remove(String key);
    public void expire(String key, long expireTimeInMilliSecond);
    public Set<Map.Entry<String, Object>> entrySetCopy();
}
