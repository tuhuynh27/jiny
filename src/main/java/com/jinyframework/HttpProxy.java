package com.jinyframework;

import com.jinyframework.core.factories.ServerThreadFactory;
import com.jinyframework.core.utils.Intro;
import com.jinyframework.core.utils.ParserUtils;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
@RequiredArgsConstructor
public final class HttpProxy {
    private final int proxyPort;
    private final Executor executor = Executors.newSingleThreadExecutor(
            new ServerThreadFactory("proxy-processor"));
    private final Map<String, String> endpointMap = new HashMap<>();

    public static HttpProxy port(final int proxyPort) {
        return new HttpProxy(proxyPort);
    }

    public void use(@NonNull final String path, @NonNull final String endpoint) {
        endpointMap.put(path, endpoint);
    }

    public void start() throws IOException {
        Intro.begin();
        val serverSocket = new ServerSocket(proxyPort);
        log.info("Started Jiny HTTP Server on port " + proxyPort);
        while (!Thread.interrupted()) {
            val clientSocket = serverSocket.accept();
            executor.execute(new ProxyHandler(clientSocket));
        }
    }

    @RequiredArgsConstructor
    public class ProxyHandler implements Runnable {
        private final Socket clientSocket;

        // TODO: Resolve proxy keep alive issue

        @SneakyThrows
        @Override
        public void run() {
            try {
                @Cleanup val clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
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

                // TODO: Improve matching algorithm, this is native
                var matchedKey = endpointMap.keySet().stream()
                        .filter(path::startsWith).findFirst()
                        .orElse(null);

                if (matchedKey == null) {
                    if (endpointMap.get("/") != null) {
                        matchedKey = "/";
                    }
                }

                if (matchedKey != null) {
                    val endpoint = endpointMap.get(matchedKey);
                    val serverMetadata = endpoint.split(":");
                    val serverSocket = new Socket(serverMetadata[0], Integer.parseInt(serverMetadata[1]));

                    @Cleanup val clientOut = clientSocket.getOutputStream();
                    @Cleanup val serverIn = serverSocket.getInputStream();
                    @Cleanup val serverOut = new PrintWriter(serverSocket.getOutputStream(), false);

                    // Replace path
                    if (!path.toLowerCase().equals(matchedKey.toLowerCase())) {
                        requestStringArr.set(0, requestStringArr.get(0).replace(matchedKey, ""));
                    }

                    val requestStr = String.join("\r\n", requestStringArr) + "\r\n\r\n" + body;

                    serverOut.write(requestStr);
                    serverOut.flush();

                    val reply = new byte[4096];
                    var bytesRead = 0;
                    while (-1 != (bytesRead = serverIn.read(reply))) {
                        clientOut.write(reply, 0, bytesRead);
                    }
                    clientOut.flush();

                    serverSocket.close();
                }

                if (!clientSocket.isClosed()) {
                    val clientOut = new PrintWriter(clientSocket.getOutputStream(), false);
                    clientOut.write("HTTP/1.1 404 Not Found\n\nNot Found\n");
                    clientOut.flush();
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                if (!clientSocket.isClosed()) {
                    val clientOut = new PrintWriter(clientSocket.getOutputStream(), false);
                    clientOut.write("HTTP/1.1 500 Internal Server Error\n\nHttpProxy Error: " + e.getMessage() + "\n");
                    clientOut.flush();
                }
            } finally {
                clientSocket.close();
            }
        }
    }

}
