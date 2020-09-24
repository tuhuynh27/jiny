package com.tuhuynh.jerrymouse.proxy;

import lombok.val;
import lombok.var;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public final class Proxy {
    public static void main(String[] args) throws IOException {
        val serverSocket = new ServerSocket(7777);
        while (!Thread.interrupted()) {
            val clientSocket = serverSocket.accept();
            val serverConnection = new Socket("127.0.0.1", 1234);
            handle(clientSocket, serverConnection);
        }
    }

    public static void handle(final Socket clientSocket, final Socket serverSocket) throws IOException {
        val clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        val clientOut = clientSocket.getOutputStream();
        val serverIn = serverSocket.getInputStream();
        val serverOut = new PrintWriter(serverSocket.getOutputStream(), false);

        val requestStringArr = new ArrayList<String>();
        String inputLine;
        while (!(inputLine = clientIn.readLine()).isEmpty()) {
            requestStringArr.add(inputLine);
        }
        val body = new StringBuilder();
        while (clientIn.ready()) {
            body.append((char) clientIn.read());
        }

        val requestStr = String.join("\r\n", requestStringArr) + "\r\n\r\n" + body;

        serverOut.write(requestStr);
        serverOut.flush();

        val reply = new byte[4096];
        var bytesRead = 0;
        while (-1 != (bytesRead = serverIn.read(reply))) {
            clientOut.write(reply, 0, bytesRead);
        }

        clientSocket.close();
        serverSocket.close();
    }
}
