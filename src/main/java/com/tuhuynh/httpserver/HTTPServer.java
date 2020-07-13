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

import lombok.val;

public final class HTTPServer {
    public static HTTPServer port(final int serverPort) {
        return new HTTPServer(serverPort);
    }

    private final int serverPort;
    private final Executor executor = Executors.newCachedThreadPool();
    private ArrayList<HandlerMetadata> handlers = new ArrayList<>();

    private HTTPServer(final int serverPort) {
        this.serverPort = serverPort;
    }

    public void addHandler(final RequestMethod method, final String path, final RequestHandler... handlers) {
        val newHandlers = HandlerMetadata.builder().method(method).path(path).handlers(handlers).build();
        this.handlers.add(newHandlers);
    }

    public void use(final String path, final RequestHandler... handlers) {
        val newHandlers = HandlerMetadata.builder().method(RequestMethod.ALL).path(path).handlers(handlers)
                                         .build();
        this.handlers.add(newHandlers);
    }

    public void get(final String path, final RequestHandler... handlers) {
        val newHandlers = HandlerMetadata.builder().method(RequestMethod.GET).path(path).handlers(handlers)
                                         .build();
        this.handlers.add(newHandlers);
    }

    public void post(final String path, final RequestHandler... handlers) {
        val newHandlers = HandlerMetadata.builder().method(RequestMethod.POST).path(path).handlers(handlers)
                                         .build();
        this.handlers.add(newHandlers);
    }

    public void put(final String path, final RequestHandler... handlers) {
        val newHandlers = HandlerMetadata.builder().method(RequestMethod.PUT).path(path).handlers(handlers)
                                         .build();
        this.handlers.add(newHandlers);
    }

    public void delete(final String path, final RequestHandler... handlers) {
        val newHandlers = HandlerMetadata.builder().method(RequestMethod.DELETE).path(path).handlers(handlers)
                                         .build();
        this.handlers.add(newHandlers);
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
