package com.jinyframework.keva.command;

import lombok.val;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;

import static com.jinyframework.keva.ServiceFactory.connectionService;

public class Stat implements CommandHandler {
    @Override
    public Object handle(List<String> args) {
        val stats = new HashMap<String, Object>();
        val currentConnectedClients = connectionService().getCurrentConnectedClients();
        val threads = ManagementFactory.getThreadMXBean().getThreadCount();
        stats.put("clients:", currentConnectedClients);
        stats.put("threads:", threads);
        return stats;
    }
}
