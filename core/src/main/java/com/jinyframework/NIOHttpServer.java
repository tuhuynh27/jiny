package com.jinyframework;

import com.jinyframework.core.AbstractHttpRouter;
import com.jinyframework.core.AbstractRequestBinder.HandlerNIO;
import com.jinyframework.core.AbstractRequestBinder.RequestTransformer;
import com.jinyframework.core.factories.ServerThreadFactory;
import com.jinyframework.core.nio.RequestPipelineNIO;
import com.jinyframework.core.utils.Intro;
import lombok.NonNull;
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
import java.util.Map;

/**
 * The type Nio http server.
 */
@Slf4j
public final class NIOHttpServer extends AbstractHttpRouter<HandlerNIO> {
    private final int serverPort;
    private final ServerThreadFactory threadFactory = new ServerThreadFactory("event-loop");
    private AsynchronousServerSocketChannel serverSocketChannel;
    private int maxThread = Runtime.getRuntime().availableProcessors() * 2;

    private NIOHttpServer(final int serverPort) {
        this.serverPort = serverPort;
    }

    /**
     * Port nio http server.
     *
     * @param serverPort the server port
     * @return the nio http server
     */
    public static NIOHttpServer port(final int serverPort) {
        return new NIOHttpServer(serverPort);
    }

    /**
     * Use transformer nio http server.
     *
     * @param transformer the transformer
     * @return the nio http server
     */
    public NIOHttpServer useTransformer(@NonNull final RequestTransformer transformer) {
        this.transformer = transformer;
        return this;
    }

    /**
     * Use response headers http server.
     *
     * @param responseHeaders the response headers
     * @return the http server
     */
    public NIOHttpServer useResponseHeaders(@NonNull final Map<String, String> responseHeaders) {
        this.responseHeaders = responseHeaders;
        return this;
    }

    /**
     * Sets num of event loop thread.
     *
     * @param maxThread the max thread
     * @return the num of event loop thread
     * @throws IOException the io exception
     */
    public NIOHttpServer setNumOfEventLoopThread(final int maxThread) throws IOException {
        if (maxThread < 1) {
            throw new ArithmeticException("maxThread cannot lower than 1");
        }
        this.maxThread = maxThread;
        return this;
    }

    /**
     * Sets thread debug mode.
     *
     * @param isDebug the is debug
     * @return the thread debug mode
     */
    public NIOHttpServer setThreadDebugMode(final boolean isDebug) {
        threadFactory.setDebug(isDebug);
        return this;
    }

    /**
     * Start.
     */
    public void start() {
        Intro.begin();
        try {
            val group = AsynchronousChannelGroup.withFixedThreadPool(maxThread, threadFactory);
            serverSocketChannel = AsynchronousServerSocketChannel.open(group);
            serverSocketChannel.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), serverPort));
            log.info("Started NIO HTTP Server on port " + serverPort + " using " + maxThread + " event loop thread(s)");
            serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
                @SneakyThrows
                @Override
                public void completed(AsynchronousSocketChannel clientSocketChannel, Object attachment) {
                    serverSocketChannel.accept(null, this);
                    new RequestPipelineNIO(clientSocketChannel, middlewares, handlers, responseHeaders, transformer).run();
                }

                @Override
                public void failed(Throwable e, Object attachment) {
                    log.error(e.getMessage(), e);
                }
            });
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Stop.
     */
    public void stop() {
        if (serverSocketChannel.isOpen()) {
            try {
                serverSocketChannel.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
            log.info("Stopped Jiny HTTP Server on port " + serverPort);
        }
    }
}
