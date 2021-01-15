package com.jinyframework.keva.core;

import com.jinyframework.keva.command.CommandService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@Slf4j
public final class ConnectionService {
    private ConnectionService() {
    }

    public static void handleConnection(Socket socket) {
        try {
            val socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            val socketOut = new PrintWriter(socket.getOutputStream());
            while (!socket.isClosed()) {
                val line = socketIn.readLine();
                if (line == null || line.isEmpty() || "null".equals(line)) {
                    continue;
                }
                log.info("Received from client: " + line);
                CommandService.handleCommand(socketOut, line);
            }
            socket.close();
            socketIn.close();
            socketOut.close();
        } catch (Exception e) {
            log.error("Error occurred while handling socket: ", e);
        }
    }
}
