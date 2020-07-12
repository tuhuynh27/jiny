package com.tuhuynh.httpserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.tuhuynh.httpserver.handlers.HandlerBinder.HandlerMetadata;
import com.tuhuynh.httpserver.handlers.HandlerBinder.RequestHandler;
import com.tuhuynh.httpserver.handlers.HandlerPipeline;
import com.tuhuynh.httpserver.utils.HandlerUtils.RequestMethod;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public final class HTTPServer {
    private final int serverPort;
    private final Executor executor = Executors.newCachedThreadPool();
    private ArrayList<HandlerMetadata> handlers = new ArrayList<>();

    public void addHandler(final RequestMethod method, final String path, final RequestHandler... handler) {
        val newHandler = HandlerMetadata.builder().method(method).path(path).handler(handler).build();
        handlers.add(newHandler);
    }

    public void start() throws IOException {
        val serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(InetAddress.getLocalHost(), serverPort));

        for (; ; ) {
            val socket = serverSocket.accept();
            executor.execute(new HandlerPipeline(socket, handlers));
        }
    }
}
