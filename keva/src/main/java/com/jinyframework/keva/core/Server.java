package com.jinyframework.keva.core;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.Executors;

@Slf4j
@Builder
public class Server {
    private final String host;
    private final int port;

    public void run() throws IOException {
        val executor = Executors.newFixedThreadPool(4);
        val serverSocket = new ServerSocket();
        val socketAddress = new InetSocketAddress(host, port);
        serverSocket.bind(socketAddress);
        log.info("Database server started");
        while (!Thread.interrupted()) {
            val socket = serverSocket.accept();
            executor.execute(() -> {
                ConnectionService.handleConnection(socket);
            });
        }
    }
}
