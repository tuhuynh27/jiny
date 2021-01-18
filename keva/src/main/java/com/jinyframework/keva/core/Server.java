package com.jinyframework.keva.core;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Builder
public class Server {
    private final String host;
    private final int port;
    private ServerSocket serverSocket;
    private ExecutorService executor;

    private void init() throws IOException {
        executor = Executors.newFixedThreadPool(4);
        val socketAddress = new InetSocketAddress(host, port);
        if (serverSocket == null) {
            serverSocket = new ServerSocket();
        }
        serverSocket.bind(socketAddress);
        log.info("Database server started");
    }

    public void run() throws IOException {
        init();
        while (!Thread.interrupted()) {
            val socket = serverSocket.accept();
            log.info("{} connected",socket.getRemoteSocketAddress());
            executor.execute(() -> {
                ConnectionService.handleConnection(socket);
            });
        }
    }

    public void shutdown() throws Exception {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        val graceful = executor.awaitTermination(5, TimeUnit.SECONDS);
        if (!graceful) {
            log.error("Graceful shutdown timed out");
        }
        log.info("Database server stopped");
    }
}
