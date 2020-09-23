package com.tuhuynh.jerrymouse.core;

import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

public class ServerThreadFactory implements ThreadFactory {
    private int counter;
    private String name;
    private List<String> stats;

    public ServerThreadFactory(String name) {
        counter = 1;
        this.name = name;
        stats = new ArrayList<>();
    }

    @Override
    public Thread newThread(Runnable runnable) {
        val t = new Thread(runnable, name + "-thread-" + counter);
        counter++;
        stats.add(String.format("Created thread %d with name %s on %d \n", t.getId(), t.getName(),
                                System.currentTimeMillis() / 1000L));
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