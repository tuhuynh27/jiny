package com.jinyframework;

import com.jinyframework.core.RequestBinderBase.HandlerNIO;
import com.jinyframework.core.RequestBinderBase.RequestTransformer;
import com.jinyframework.core.HttpRouterBase;
import com.jinyframework.core.ServerThreadFactory;
import com.jinyframework.core.nio.RequestPipeline;
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

public final class NIOHttpServer extends HttpRouterBase<HandlerNIO> {
    private final int serverPort;
    private AsynchronousServerSocketChannel serverSocketChannel;

    private RequestTransformer transformer = Object::toString;

    private NIOHttpServer(final int serverPort) {
        this.serverPort = serverPort;
    }

    public static NIOHttpServer port(final int serverPort) {
        return new NIOHttpServer(serverPort);
    }

    public void setupResponseTransformer(RequestTransformer transformer) {
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
            System.out.println("Stopped HTTP Server on port " + serverPort);
        }
    }
}
