package com.jinyframework.core;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

@Slf4j
public final class ServerThreadFactory implements ThreadFactory {
    private final String name;
    private final List<String> stats;
    private int counter;

    public ServerThreadFactory(String name) {
        counter = 1;
        this.name = name;
        stats = new ArrayList<>();
    }

    @Override
    public Thread newThread(Runnable runnable) {
        val t = new Thread(runnable, name + "-thread-" + counter);
        counter++;
        val info = String.format("Created thread %d with name %s on %d \n", t.getId(), t.getName(),
                System.currentTimeMillis() / 1000L);
        stats.add(info);
        log.info(info);
        return t;
    }

    public String getStats() {
        val buffer = new StringBuilder();
        for (String stat : stats) {
            buffer.append(stat);
        }
        return buffer.toString();
    }
}