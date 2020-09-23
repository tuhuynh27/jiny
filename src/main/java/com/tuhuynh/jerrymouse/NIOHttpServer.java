package com.tuhuynh.jerrymouse;

import com.tuhuynh.jerrymouse.core.ParserUtils.RequestMethod;
import com.tuhuynh.jerrymouse.core.RequestBinder.BaseHandlerMetadata;
import com.tuhuynh.jerrymouse.core.RequestBinder.NIOHandlerMetadata;
import com.tuhuynh.jerrymouse.core.RequestBinder.RequestHandlerNIO;
import com.tuhuynh.jerrymouse.core.ServerThreadFactory;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public final class NIOHttpServer {
    public static NIOHttpServer port(final int serverPort) {
        return new NIOHttpServer(serverPort);
    }

    private final int serverPort;
    private final ArrayList<RequestHandlerNIO> middlewares = new ArrayList<>();
    private final ArrayList<BaseHandlerMetadata<RequestHandlerNIO>> handlers = new ArrayList<>();
    private AsynchronousServerSocketChannel serverSocketChannel;

    private NIOHttpServer(final int serverPort) {
        this.serverPort = serverPort;
    }

    public void addHandler(final RequestMethod method, final String path,
                           final RequestHandlerNIO... handlers) {
        val newHandlers = new NIOHandlerMetadata(method, path, handlers);
        this.handlers.add(newHandlers);
    }

    public void use(final RequestHandlerNIO... handlers) {
        middlewares.addAll(Arrays.stream(handlers)
                                 .collect(Collectors.toCollection(ArrayList::new)));
    }

    public void use(final String path, final RequestHandlerNIO... handlers) {
        val newHandlers = new NIOHandlerMetadata(RequestMethod.ALL, path, handlers);
        this.handlers.add(newHandlers);
    }

    public void get(final String path, final RequestHandlerNIO... handlers) {
        val newHandlers = new NIOHandlerMetadata(RequestMethod.GET, path, handlers);
        this.handlers.add(newHandlers);
    }

    public void post(final String path, final RequestHandlerNIO... handlers) {
        val newHandlers = new NIOHandlerMetadata(RequestMethod.POST, path, handlers);
        this.handlers.add(newHandlers);
    }

    public void put(final String path, final RequestHandlerNIO... handlers) {
        val newHandlers = new NIOHandlerMetadata(RequestMethod.PUT, path, handlers);
        this.handlers.add(newHandlers);
    }

    public void delete(final String path, final RequestHandlerNIO... handlers) {
        val newHandlers = new NIOHandlerMetadata(RequestMethod.DELETE, path, handlers);
        this.handlers.add(newHandlers);
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
                new RequestPipeline(clientSocketChannel, middlewares, handlers).run();
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
