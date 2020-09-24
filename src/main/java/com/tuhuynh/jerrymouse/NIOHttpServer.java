package com.tuhuynh.jerrymouse;

import com.tuhuynh.jerrymouse.core.HttpRouterBase;
import com.tuhuynh.jerrymouse.core.RequestBinderBase.RequestHandlerNIO;
import com.tuhuynh.jerrymouse.core.RequestBinderBase.RequestTransformer;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public final class NIOHttpServer extends HttpRouterBase<RequestHandlerNIO> {
    private final int serverPort;
    private AsynchronousServerSocketChannel serverSocketChannel;

    private RequestTransformer transformer = Object::toString;

    private NIOHttpServer(final int serverPort) {
        this.serverPort = serverPort;
    }

    public static NIOHttpServer port(final int serverPort) {
        return new NIOHttpServer(serverPort);
    }

    public void setResponseTransformer(RequestTransformer transformer) {
        this.transformer = transformer;
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
                new RequestPipeline(clientSocketChannel, middlewares, handlers, transformer).run();
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
