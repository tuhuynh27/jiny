package com.tuhuynh.jerrymouse;

import com.tuhuynh.jerrymouse.core.ParserUtils.RequestMethod;
import com.tuhuynh.jerrymouse.core.RequestBinder.RequestHandlerNIO;
import com.tuhuynh.jerrymouse.core.ServerThreadFactory;
import com.tuhuynh.jerrymouse.core.nio.HttpRouter;
import com.tuhuynh.jerrymouse.core.nio.RequestPipeline;
import lombok.SneakyThrows;
import lombok.val;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public final class NIOHttpServer {
    private final int serverPort;
    private final HttpRouter rootRouter = new HttpRouter();
    private AsynchronousServerSocketChannel serverSocketChannel;

    private NIOHttpServer(final int serverPort) {
        this.serverPort = serverPort;
    }

    public static NIOHttpServer port(final int serverPort) {
        return new NIOHttpServer(serverPort);
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

    public void addHandler(final RequestMethod method, final String path,
                           final RequestHandlerNIO... handlers) {
        rootRouter.addHandler(method, path, handlers);
    }

    public void use(final RequestHandlerNIO... handlers) {
        rootRouter.use(handlers);
    }

    public void use(final String path, final RequestHandlerNIO... handlers) {
        rootRouter.use(path, handlers);
    }

    public void get(final String path, final RequestHandlerNIO... handlers) {
        rootRouter.get(path, handlers);
    }

    public void post(final String path, final RequestHandlerNIO... handlers) {
        rootRouter.post(path, handlers);
    }

    public void put(final String path, final RequestHandlerNIO... handlers) {
        rootRouter.put(path, handlers);
    }

    public void delete(final String path, final RequestHandlerNIO... handlers) {
        rootRouter.delete(path, handlers);
    }

    public void start() throws IOException, InterruptedException, ExecutionException, TimeoutException {
        val group = AsynchronousChannelGroup.withFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2,
                new ServerThreadFactory("event-loop"));
        serverSocketChannel = AsynchronousServerSocketChannel.open(group);
        serverSocketChannel.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), serverPort));
        System.out.println("Started NIO HTTP Server on port " + serverPort);
        serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
            @SneakyThrows
            @Override
            public void completed(AsynchronousSocketChannel clientSocketChannel, Object attachment) {
                serverSocketChannel.accept(null, this);
                new RequestPipeline(clientSocketChannel, rootRouter.getMiddlewares(), rootRouter.getHandlers()).run();
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                System.out.println(exc.getMessage());
            }
        });
    }

    public void stop() throws IOException {
        if (serverSocketChannel.isOpen()) {
            serverSocketChannel.close();
        }
    }
}
