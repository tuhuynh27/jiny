package com.jinyframework.keva;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@Slf4j
@Builder
public class KevaClient {
    private final String host;
    private final int port;
    private PrintWriter socketOut;
    private Socket socket;
    private BufferedReader socketIn;
    private BufferedReader consoleIn;

    public void init() throws Exception {
        consoleIn = new BufferedReader(new InputStreamReader(System.in));
        socket = new Socket(host, port);
        socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        socketOut = new PrintWriter(socket.getOutputStream());
        log.info("Client Started");
        log.info("Press 'q' to quit client");
    }

    public void run() throws Exception {
        init();
        while (!socket.isClosed()) {
            val consoleLine = consoleIn.readLine();
            if (consoleLine == null || consoleLine.isEmpty()) {
                continue;
            }

            if ("q".equals(consoleLine)) {
                break;
            }
            socketOut.println(consoleLine);
            socketOut.flush();
            val line = socketIn.readLine();
            log.info("Received from server: {}", line);
        }
    }

    public void shutdown() throws Exception {
        consoleIn.close();
        socketIn.close();
        socketOut.close();
        socket.close();
        log.info("Bye");
    }
}
