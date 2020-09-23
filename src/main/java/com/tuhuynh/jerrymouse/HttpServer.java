package com.tuhuynh.jerrymouse;

import com.tuhuynh.jerrymouse.core.ParserUtils.RequestMethod;
import com.tuhuynh.jerrymouse.core.RequestBinder.RequestHandlerBIO;
import com.tuhuynh.jerrymouse.core.ServerThreadFactory;
import com.tuhuynh.jerrymouse.core.bio.HttpRouter;
import com.tuhuynh.jerrymouse.core.bio.RequestPipeline;
import lombok.val;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public final class HttpServer {
    public static HttpServer port(final int serverPort) {
        return new HttpServer(serverPort);
    }

    private final int serverPort;
    private final Executor executor = Executors.newCachedThreadPool(
            new ServerThreadFactory("request-processor"));
    private ServerSocket serverSocket;

    private final HttpRouter rootRouter = new HttpRouter();

    private HttpServer(final int serverPort) {
        this.serverPort = serverPort;
    }

    public void use(final String path, final HttpRouter router) {
        val refactoredMiddlewares = router.getMiddlewares().stream().peek(e -> {
            val refactoredPath = path + e.getPath();
            e.setPath(refactoredPath);
        }).collect(Collectors.toList());
        val refactoredHandlers = router.getHandlers().stream().peek(e -> {
            val refactoredPath = path + e.getPath();
            e.setPath(refactoredPath);
        }).collect(Collectors.toList());
        rootRouter.getMiddlewares().addAll(refactoredMiddlewares);
        rootRouter.getHandlers().addAll(refactoredHandlers);
    }

    public void addHandler(final RequestMethod method, final String path, final RequestHandlerBIO... handlers) {
        rootRouter.addHandler(method, path, handlers);
    }

    public void use(final RequestHandlerBIO... handlers) {
        rootRouter.use(handlers);
    }

    public void use(final String path, final RequestHandlerBIO... handlers) {
        rootRouter.addHandler(RequestMethod.ALL, path, handlers);
    }

    public void get(final String path, final RequestHandlerBIO... handlers) {
        rootRouter.addHandler(RequestMethod.GET, path, handlers);
    }

    public void post(final String path, final RequestHandlerBIO... handlers) {
        rootRouter.addHandler(RequestMethod.POST, path, handlers);
    }

    public void put(final String path, final RequestHandlerBIO... handlers) {
        rootRouter.addHandler(RequestMethod.PUT, path, handlers);
    }

    public void delete(final String path, final RequestHandlerBIO... handlers) {
        rootRouter.addHandler(RequestMethod.DELETE, path, handlers);
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), serverPort));
        System.out.println("Started HTTP Server on port " + serverPort);

        while (!serverSocket.isClosed()) {
            val socket = serverSocket.accept();
            executor.execute(new RequestPipeline(socket, rootRouter.getMiddlewares(), rootRouter.getHandlers()));
        }
    }

    public void stop() throws IOException {
        if (!serverSocket.isClosed()) {
            serverSocket.close();
            System.out.println("Stopped HTTP Server on port " + serverPort);
        }
    }
}
