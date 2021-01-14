package com.jinyframework.keva.core;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.Executors;

@Slf4j
@Builder
public class Server {
    private final String host;
    private final int port;
    private final Map<String, Object> mapStore;
    private final CommandService commandService;


    public void run() throws IOException {
        val executor = Executors.newFixedThreadPool(4);
        val serverSocket = new ServerSocket();
        val socketAddress = new InetSocketAddress(host, port);
        serverSocket.bind(socketAddress);
        log.info("Database server started");
        while (!Thread.interrupted()) {
            val socket = serverSocket.accept();
            executor.execute(() -> {
                try {
                    val socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    val socketOut = new PrintWriter(socket.getOutputStream());
                    while (!socket.isClosed()) {
                        val line = socketIn.readLine();
                        if (line == null || line.isEmpty() || "null".equals(line)) {
                            continue;
                        }
                        log.info("Received from client: " + line);
                        commandService.handleCommand(socketOut, line);
                    }
                    socket.close();
                    socketIn.close();
                    socketOut.close();
                } catch (Exception e) {
                    log.error("Error occurred while handling socket: ", e);
                }
            });
        }
    }
}
