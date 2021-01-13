package com.jinyframework.keva;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@Slf4j
public final class Client {
    public static void main(String[] args) throws Exception {
        try {
            val consoleIn = new BufferedReader(new InputStreamReader(System.in));
            // Communication Endpoint for client and server
            val socket = new Socket("LocalHost", 6767);
            log.info("Client Started");
            log.info("Press 'q' to quit client");
            val socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            val socketOut = new PrintWriter(socket.getOutputStream());
            while (!socket.isClosed()) {
                val consoleLine = consoleIn.readLine();
                if (consoleLine == null || consoleLine.isEmpty()) {
                    continue;
                }

                if ("q".equals(consoleLine)) {
                    socketOut.println("Client is shutting down...");
                    break;
                }
                socketOut.println(consoleLine);
                socketOut.flush();
                val line = socketIn.readLine();
                log.info("Received from server: {}", line);
            }
            socket.close();
            socketIn.close();
            socketOut.close();
        } catch (Exception e) {
            log.error("{} {}", e.getMessage(), e.getCause());
        }
    }
}
