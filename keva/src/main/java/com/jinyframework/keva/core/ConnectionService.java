package com.jinyframework.keva.core;

import com.jinyframework.keva.ServiceFactory;
import com.jinyframework.keva.command.CommandService;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ConnectionService {
    private final CommandService commandService = ServiceFactory.commandService();

    private final Map<String, KevaSocket> socketMap = new ConcurrentHashMap<>();

    public void handleConnection(KevaSocket kevaSocket) {
        try {
            socketMap.put(kevaSocket.getId(), kevaSocket);
            val socket = kevaSocket.getSocket();
            log.info("{} {} connected", socket.getRemoteSocketAddress(), kevaSocket.getId());

            @Cleanup
            val socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            @Cleanup
            val socketOut = new PrintWriter(socket.getOutputStream());
            while (!socket.isClosed() && kevaSocket.isAlive()) {
                val line = socketIn.readLine();
                if (line == null || line.isEmpty() || "null".equals(line)) {
                    continue;
                }
                kevaSocket.getLastOnlineLong().set(System.currentTimeMillis());
                log.info("Received from client: " + line);
                commandService.handleCommand(socketOut, line);
            }
        } catch (Exception e) {
            log.error("Error occurred while handling socket: ", e);
        } finally {
            socketMap.remove(kevaSocket.getId());
            log.info("{} {} disconnected", kevaSocket.getSocket().getRemoteSocketAddress(), kevaSocket.getId());
        }
    }

    public long getCurrentConnectedClients() {
        return socketMap.size();
    }

    public Runnable getHeartbeatRunnable(long sockTimeout) {
        return () -> {
            log.info("Running heartbeat");
            val now = System.currentTimeMillis();
            socketMap.values().forEach(kevaSocket -> {
                if (kevaSocket.getLastOnline() + sockTimeout < now) {
                    kevaSocket.getAlive().set(false);
                }
            });
        };
    }
}
