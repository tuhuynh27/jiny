package com.jinyframework;

import com.jinyframework.core.HttpRouterBase;
import com.jinyframework.core.RequestBinderBase.HandlerNIO;
import com.jinyframework.core.RequestBinderBase.RequestTransformer;
import com.jinyframework.core.ServerThreadFactory;
import com.jinyframework.core.nio.RequestPipeline;
import com.jinyframework.core.utils.Intro;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
        Intro.begin();
        val group = AsynchronousChannelGroup.withFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2,
                new ServerThreadFactory("event-loop"));
        serverSocketChannel = AsynchronousServerSocketChannel.open(group);
        serverSocketChannel.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), serverPort));
        log.info("Started NIO HTTP Server on port " + serverPort);
        serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
            @SneakyThrows
            @Override
            public void completed(AsynchronousSocketChannel clientSocketChannel, Object attachment) {
                serverSocketChannel.accept(null, this);
                new RequestPipeline(clientSocketChannel, middlewares, handlers, transformer).run();
            }

            @Override
            public void failed(Throwable e, Object attachment) {
                log.error(e.getMessage(), e);
            }
        });
    }

    public void stop() throws IOException {
        if (serverSocketChannel.isOpen()) {
            serverSocketChannel.close();
            log.info("Stopped Jiny HTTP Server on port " + serverPort);
        }
    }
}
