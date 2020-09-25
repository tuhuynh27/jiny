package com.tuhuynh.jerrymouse.proxy;

import com.tuhuynh.jerrymouse.core.utils.ParserUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.var;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

@RequiredArgsConstructor
public final class Proxy {
    private final int proxyPort;
    private final HashMap<String, String> endpointMap = new HashMap<>();

    public static Proxy port(final int proxyPort) {
        return new Proxy(proxyPort);
    }

    public void use(@NonNull final String path, @NonNull final String endpoint) {
        endpointMap.put(path, endpoint);
    }

    public void start() throws IOException {
        val serverSocket = new ServerSocket(proxyPort);
        while (!Thread.interrupted()) {
            val clientSocket = serverSocket.accept();
            handle(clientSocket);
        }
    }

    public void handle(final Socket clientSocket) throws IOException {
        val clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        val requestStringArr = new ArrayList<String>();
        String inputLine;
        while (!(inputLine = clientIn.readLine()).isEmpty()) {
            requestStringArr.add(inputLine);
        }
        val body = new StringBuilder();
        while (clientIn.ready()) {
            body.append((char) clientIn.read());
        }

        // Pick server
        val requestContext = ParserUtils.parseRequest(requestStringArr.toArray(new String[0]),
                body.toString());
        val path = requestContext.getPath();

        val matchedKey = endpointMap.keySet().stream()
                .filter(path::startsWith).findFirst()
                .orElse(null);

        if (matchedKey != null) {
            val endpoint = endpointMap.get(matchedKey);
            val serverMetadata = endpoint.split(":");
            try {
                val serverSocket = new Socket(serverMetadata[0], Integer.parseInt(serverMetadata[1]));

                val serverIn = serverSocket.getInputStream();
                val serverOut = new PrintWriter(serverSocket.getOutputStream(), false);

                val clientOut = clientSocket.getOutputStream();

                // Replace path
                requestStringArr.set(0, requestStringArr.get(0).replace(matchedKey, ""));
                val requestStr = String.join("\r\n", requestStringArr) + "\r\n\r\n" + body;

                serverOut.write(requestStr);
                serverOut.flush();

                val reply = new byte[4096];
                var bytesRead = 0;
                while (-1 != (bytesRead = serverIn.read(reply))) {
                    clientOut.write(reply, 0, bytesRead);
                }

                serverSocket.close();
            } catch (Exception ignored) {
                val clientOut = new PrintWriter(clientSocket.getOutputStream(), false);
                clientOut.write("HTTP/1.1 404 Not Found\n\nNot Found\n");
                clientSocket.close();
            }
        }

        if (!clientSocket.isClosed()) {
            val clientOut = new PrintWriter(clientSocket.getOutputStream(), false);
            clientOut.write("HTTP/1.1 404 Not Found\n\nNot Found\n");

            clientSocket.close();
        }
    }
}
