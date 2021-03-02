package com.jinyframework.keva.server.noheap;

public interface HashStore {
    void put(String k, Long v);

    Long get(String k);

    void remove(String k);

    int getCollisions();

    int getLoad();

    void outputStats();

    void reset();
}
