package com.jinyframework.keva;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

@Slf4j
public final class Server {
    static String[] tokenized(String line) {
        return line.split(" ");
    }

    public static void main(String[] args) {
        try {
            val mapStore = new ConcurrentHashMap<>();

            val executor = Executors.newFixedThreadPool(4);
            val serverHost = "localhost";
            val serverPort = 6767;
            val serverSocket = new ServerSocket();
            val socketAddress = new InetSocketAddress(serverHost, serverPort);
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
                            val tokens = tokenized(line);
                            switch (tokens[0]) {
                                case "get":
                                    socketOut.println(mapStore.get(tokens[1]));
                                    break;
                                case "set":
                                    mapStore.put(tokens[1], tokens[2]);
                                    socketOut.println(1);
                                    break;
                                case "ping":
                                    socketOut.println("pong");
                                    break;
                                default:
                                    socketOut.println("Unsupported command");
                            }
                            socketOut.flush();
                            log.info("Received from client: " + line);
                        }
                        socket.close();
                        socketIn.close();
                        socketOut.close();
                    } catch (Exception e) {
                        log.error("{} {}", e.getMessage(), e.getCause());
                    }
                });
            }
        } catch (Exception e) {
            log.error("{} {}", e.getMessage(), e.getCause());
        }
    }
}
