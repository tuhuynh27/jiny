package com.jinyframework;

import com.jinyframework.core.HttpRouterBase;
import com.jinyframework.core.RequestBinderBase.Handler;
import com.jinyframework.core.RequestBinderBase.RequestTransformer;
import com.jinyframework.core.bio.RequestPipeline;
import com.jinyframework.core.factories.ServerThreadFactory;
import com.jinyframework.core.utils.Intro;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import lombok.val;

@Slf4j
public final class HttpServer extends HttpRouterBase<Handler> {
    private final int serverPort;
    private final ServerThreadFactory threadFactory = new ServerThreadFactory("request-processor");
    private final Executor executor = Executors.newCachedThreadPool(threadFactory);
    private ServerSocket serverSocket;

    private HttpServer(final int serverPort) {
        this.serverPort = serverPort;
    }

    public static HttpServer port(final int serverPort) {
        return new HttpServer(serverPort);
    }

    public HttpServer useTransformer(@NonNull final RequestTransformer transformer) {
        this.transformer = transformer;
        return this;
    }

    public HttpServer setThreadDebugMode(final boolean isDebug) {
        threadFactory.setDebug(isDebug);
        return this;
    }

    public void start() throws IOException {
        Intro.begin();
        serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), serverPort));
        log.info("Started Jiny HTTP Server on port " + serverPort);
        while (!Thread.interrupted()) {
            val clientSocket = serverSocket.accept();
            executor.execute(
                    new RequestPipeline(clientSocket, middlewares, handlers, transformer));
        }
    }

    public void stop() throws IOException {
        if (!serverSocket.isClosed()) {
            serverSocket.close();
            log.info("Stopped Jiny HTTP Server on port " + serverPort);
        }
    }
}
